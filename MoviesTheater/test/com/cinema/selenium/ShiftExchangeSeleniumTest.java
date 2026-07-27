package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;   
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end tests for the Shift Exchange use case
 * (employee requests -> Manager approves/declines, plus in-app notifications).
 * These are real browser tests: they require the app to already be running at
 * BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server database.
 *
 * Two browsers are driven at once because the flow spans two roles: the
 * employee raising the request and the Manager settling it on
 * /manager/shift-exchanges. The recipient no longer decides anything.
 *
 * Browser: Microsoft Edge (Chromium-based) via EdgeDriver. Selenium Manager
 * resolves a matching msedgedriver automatically since Edge is pre-installed
 * on Windows.
 */
public class ShiftExchangeSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";

    private static final int REQUESTER_ID = 3;   // employee@cinema.vn
    private static final String REQUESTER_EMAIL = "employee@cinema.vn";
    private static final String TARGET_EMAIL = "employee02@cinema.vn";
    private static final String MANAGER_EMAIL = "manager@cinema.vn";
    private static final String PASSWORD = "123456";

    // Base64(salt || SHA-256(salt || "123456")), the same stored hash the seed
    // script (cinema_booking_database (1).sql) uses for every sample account -
    // PasswordHash.verify() re-derives its own salt from this string, so reusing
    // it here logs in with PASSWORD above exactly like the other seeded accounts.
    private static final String SEED_PASSWORD_HASH =
            "GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL";

    private WebDriver driver1;
    private WebDriver driver2;
    private int targetId; // employee02@cinema.vn's AccountID, resolved/seeded in @Before

    // The seed script only ships one employee (employee@cinema.vn), but shift
    // exchange needs a second employee to hand a shift off to. Resolve
    // employee02@cinema.vn if it already exists in the target DB, otherwise
    // create it - keeps this test independent of what's actually been run
    // against cinema_booking_database (1).sql.
    @Before
    public void ensureTargetEmployee() throws SQLException {
        try (Connection conn = DBUtils.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT AccountID FROM Account WHERE Email = ?")) {
                ps.setString(1, TARGET_EMAIL);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        targetId = rs.getInt(1);
                        return;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Account (Email, Password, RoleID) VALUES (?, ?, 3)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, TARGET_EMAIL);
                ps.setString(2, SEED_PASSWORD_HASH);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    targetId = keys.getInt(1);
                }
            }
        }
    }

    private WebDriver newDriver() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-data-dir=" + System.getProperty("java.io.tmpdir")
                + "selenium-edge-profile-" + System.nanoTime());
        return new EdgeDriver(options);
    }

    /** ShiftIDs seeded by the running test, removed again in tearDown. */
    private final List<Integer> seededShiftIds = new ArrayList<>();

    @After
    public void tearDown() {
        if (driver1 != null) driver1.quit();
        if (driver2 != null) driver2.quit();
        removeSeededShifts();
    }

    /**
     * Deletes the shifts this test inserted.
     *
     * Every test method seeds another 08:00-16:00 shift for the same employee on
     * the same date, so without this the employee's calendar collects one extra
     * copy per test method per run — they stack up on one day and make the month
     * view unreadable. ShiftExchangeRequest declares ON DELETE CASCADE on ShiftID,
     * so the requests raised against these shifts go with them.
     */
    private void removeSeededShifts() {
        if (seededShiftIds.isEmpty()) return;
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM WorkShift WHERE ShiftID = ?")) {
            for (int shiftId : seededShiftIds) {
                ps.setInt(1, shiftId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            // Cleanup must never mask the actual test result.
            e.printStackTrace();
        }
        seededShiftIds.clear();
    }

    // Inserts a fresh Scheduled shift for the requester and returns its generated ShiftID.
    // Dated tomorrow rather than today: LoginController.processRequest() calls
    // WorkShiftDAO.hasActiveShift()/checkIn() on every employee login, which
    // auto-flips a *today* shift straight to 'Completed' the instant login()
    // runs if the current time falls inside its StartTime-EndTime window -
    // and the "Chuyển ca" handoff button only renders while status is still
    // 'Scheduled'. Tomorrow's date can never match that same-day check.
    private int seedScheduledShift() throws SQLException {
        String sql = "INSERT INTO WorkShift (EmployeeID, ShiftDate, StartTime, EndTime, Status, Notes) "
                + "VALUES (?, DATEADD(DAY, 1, CAST(GETDATE() AS DATE)), '08:00', '16:00', 'Scheduled', 'Selenium UC test seed')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, REQUESTER_ID);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int shiftId = keys.getInt(1);
                seededShiftIds.add(shiftId);
                return shiftId;
            }
        }
    }

    // Reads back the RequestID of the most recent Pending request for a given shift.
    private int getPendingRequestId(int shiftId) throws SQLException {
        String sql = "SELECT TOP 1 RequestID FROM ShiftExchangeRequest "
                + "WHERE ShiftID = ? AND Status = 'Pending' ORDER BY RequestID DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void login(WebDriver driver, String email, String password) {
        login(driver, email, password, "/employee");
    }

    private void login(WebDriver driver, String email, String password, String landingPath) {
        driver.get(BASE_URL + "/Login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("#loginForm button[type=submit]")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains(landingPath));
    }

    // Reads the employee a shift currently belongs to, so a test can prove the
    // transfer really happened (or did not) rather than trusting the banner alone.
    private int shiftOwner(int shiftId) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT EmployeeID FROM WorkShift WHERE ShiftID = ?")) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Opens the hand-off form for one shift on the employee calendar. The month
    // view renders each shift as a button carrying the ShiftID, and clicking it
    // opens the detail modal whose #sm-handoff block holds the form.
    private WebElement openHandoffPanel(WebDriver driver, int shiftId) {
        driver.get(BASE_URL + "/employee/my-shifts");
        wait(driver).until(ExpectedConditions.elementToBeClickable(
                By.id("shift-" + shiftId))).click();
        return wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("sm-handoff")));
    }

    // Raises a hand-off request for the seeded shift as the requester, and returns
    // the RequestID it produced. Shared by the approve and decline tests.
    private int requestHandoff(int shiftId, String message) throws SQLException {
        driver1 = newDriver();
        login(driver1, REQUESTER_EMAIL, PASSWORD);

        WebElement panel = openHandoffPanel(driver1, shiftId);
        new Select(panel.findElement(By.name("targetEmpId"))).selectByValue(String.valueOf(targetId));
        if (message != null) {
            panel.findElement(By.name("message")).sendKeys(message);
        }
        panel.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement successBanner = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(successBanner.getText().contains("Yêu cầu chuyển ca đã được gửi"));

        return getPendingRequestId(shiftId);
    }

    // Finds the approve/decline form for one request on the Manager's queue page.
    private By decisionForm(int requestId, String action) {
        return By.xpath("//input[@name='requestId'][@value='" + requestId + "']"
                + "/ancestor::form[.//input[@name='action'][@value='" + action + "']]");
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // TC_UC_01 (Basic Flow): employee requests a hand-off, the target is told it is
    // only a proposal, and the Manager approves it - which is what actually moves
    // the shift.
    @Test
    public void testBasicFlow_requestThenManagerApproves() throws SQLException {
        int shiftId = seedScheduledShift();
        int requestId = requestHandoff(shiftId, "Selenium test: xin nhuong ca");

        // Target employee is notified, but gets no accept button: the card only
        // tells them the request is waiting on the Manager.
        driver2 = newDriver();
        login(driver2, TARGET_EMAIL, PASSWORD);
        driver2.get(BASE_URL + "/employee/my-shifts");
        WebElement badge = wait(driver2).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("cgvNotifBadge")));
        assertTrue(Integer.parseInt(badge.getText().replace("+", "")) >= 1);
        driver2.findElement(By.id("cgvNotifBellBtn")).click();
        wait(driver2).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-notif-item")));
        // Re-query the title elements fresh on every poll attempt (rather than reusing a single
        // findElements() snapshot) and ignore StaleElementReferenceException: the notification
        // list can re-render mid-check because notifications.js may still be finishing its
        // initial page-load fetch, invalidating whatever elements we grabbed earlier.
        boolean hasRequestNotification = new WebDriverWait(driver2, Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class)
                .until(dr -> dr.findElements(By.cssSelector(".cgv-notif-item-title")).stream()
                        .anyMatch(t -> t.getText().contains("Shift Exchange Proposed")));
        assertTrue(hasRequestNotification);
        assertTrue("Recipient must not be able to settle the request itself",
                driver2.findElements(decisionForm(requestId, "accept_exchange")).isEmpty());
        assertEquals("Shift must not move before the manager approves",
                REQUESTER_ID, shiftOwner(shiftId));

        // Manager approves from the review queue; only now does the shift move.
        driver2.quit();
        driver2 = newDriver();
        login(driver2, MANAGER_EMAIL, PASSWORD, "/manager");
        driver2.get(BASE_URL + "/manager/shift-exchanges?status=Pending");

        WebElement approveForm = wait(driver2).until(
                ExpectedConditions.presenceOfElementLocated(decisionForm(requestId, "approve")));
        approveForm.findElement(By.cssSelector("button[type=submit]")).click();
        driver2.switchTo().alert().accept();

        WebElement approveBanner = wait(driver2).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(approveBanner.getText().contains("Đã duyệt yêu cầu đổi ca"));
        assertEquals("Approval must transfer the shift to the target employee",
                targetId, shiftOwner(shiftId));
    }

    // AF1: attempt to hand off a shift to oneself. The dropdown never lists the
    // current user, so we inject the option via JavaScript to simulate a
    // tampered request and confirm the SERVER still rejects it.
    @Test
    public void testAlternativeFlow_selfTransferIsRejectedByServer() throws SQLException {
        int shiftId = seedScheduledShift();

        driver1 = newDriver();
        login(driver1, REQUESTER_EMAIL, PASSWORD);

        WebElement panel = openHandoffPanel(driver1, shiftId);
        WebElement select = panel.findElement(By.name("targetEmpId"));
        JavascriptExecutor js = (JavascriptExecutor) driver1;
        js.executeScript(
                "var opt = document.createElement('option'); opt.value = arguments[1]; "
                        + "arguments[0].appendChild(opt); arguments[0].value = arguments[1];",
                select, String.valueOf(REQUESTER_ID));

        panel.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement errorBanner = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger")));
        assertTrue(errorBanner.getText().contains("Không thể chuyển ca cho chính mình"));
    }

    // AF4: the Manager declines the request; the shift stays with the requester.
    @Test
    public void testAlternativeFlow_managerRejectsShiftExchange() throws SQLException {
        int shiftId = seedScheduledShift();
        int requestId = requestHandoff(shiftId, null);

        driver2 = newDriver();
        login(driver2, MANAGER_EMAIL, PASSWORD, "/manager");
        driver2.get(BASE_URL + "/manager/shift-exchanges?status=Pending");

        WebElement rejectForm = wait(driver2).until(
                ExpectedConditions.presenceOfElementLocated(decisionForm(requestId, "reject")));
        rejectForm.findElement(By.cssSelector("button[type=submit]")).click();
        driver2.switchTo().alert().accept();

        WebElement banner = wait(driver2).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Đã từ chối yêu cầu đổi ca"));
        assertEquals("A declined hand-off must leave the shift where it was",
                REQUESTER_ID, shiftOwner(shiftId));
    }

    // The employee area no longer exposes the decision actions at all, so a
    // hand-crafted POST to the old endpoint must not settle anything either.
    @Test
    public void testTamperedAcceptPostIsIgnoredByEmployeeEndpoint() throws SQLException {
        int shiftId = seedScheduledShift();
        int requestId = requestHandoff(shiftId, null);

        driver2 = newDriver();
        login(driver2, TARGET_EMAIL, PASSWORD);
        driver2.get(BASE_URL + "/employee/my-shifts");

        JavascriptExecutor js = (JavascriptExecutor) driver2;
        js.executeScript(
                "var f = document.createElement('form'); f.method = 'post';"
                        + " f.action = arguments[0] + '/employee/my-shifts';"
                        + " f.innerHTML = \"<input name='action' value='accept_exchange'>\""
                        + " + \"<input name='requestId' value='\" + arguments[1] + \"'>\";"
                        + " document.body.appendChild(f); f.submit();",
                BASE_URL, String.valueOf(requestId));

        wait(driver2).until(ExpectedConditions.urlContains("/employee/my-shifts"));
        assertEquals("An unmapped action must leave the shift untouched",
                REQUESTER_ID, shiftOwner(shiftId));
    }
}
