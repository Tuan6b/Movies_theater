package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

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

import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end tests for the Shift Exchange use case
 * (request -> accept/reject, plus in-app notifications). These are real
 * browser tests: they require the app to already be running at BASE_URL
 * (deployed via NetBeans/Tomcat) and a reachable SQL Server database.
 *
 * Browser: Microsoft Edge (Chromium-based) via EdgeDriver. Selenium Manager
 * resolves a matching msedgedriver automatically since Edge is pre-installed
 * on Windows.
 */
public class ShiftExchangeSeleniumTest {

    private static final String BASE_URL = "http://localhost:9999/MoviesTheater";

    private static final int REQUESTER_ID = 3;   // employee@cinema.vn
    private static final String REQUESTER_EMAIL = "employee@cinema.vn";
    private static final String TARGET_EMAIL = "employee02@cinema.vn";
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

    @After
    public void tearDown() {
        if (driver1 != null) driver1.quit();
        if (driver2 != null) driver2.quit();
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
                return keys.getInt(1);
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
        driver.get(BASE_URL + "/Login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("#loginForm button[type=submit]")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/employee"));
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // TC_UC_01 (Basic Flow): request an exchange, then accept it; verify both
    // flash messages and the notification bell on the target's side.
    @Test
    public void testBasicFlow_requestAndAcceptShiftExchange() throws SQLException {
        int shiftId = seedScheduledShift();

        driver1 = newDriver();
        login(driver1, REQUESTER_EMAIL, PASSWORD);
        driver1.get(BASE_URL + "/employee/my-shifts");

        wait(driver1).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#shift-wrap-" + shiftId + " button"))).click();
        WebElement panel = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#handoff-" + shiftId + ".open")));
        new Select(panel.findElement(By.name("targetEmpId"))).selectByValue(String.valueOf(targetId));
        panel.findElement(By.name("message")).sendKeys("Selenium test: xin nhuong ca");
        panel.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement successBanner = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(successBanner.getText().contains("Yêu cầu chuyển ca đã được gửi"));

        int requestId = getPendingRequestId(shiftId);

        // Target employee should see an unread notification about the new request.
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
                        .anyMatch(t -> t.getText().contains("New Shift Exchange Request")));
        assertTrue(hasRequestNotification);

        // Accept the request.
        By acceptFormLocator = By.xpath(
                "//input[@name='requestId'][@value='" + requestId + "']"
                        + "/ancestor::form[.//input[@name='action'][@value='accept_exchange']]");
        WebElement acceptForm = wait(driver2).until(ExpectedConditions.presenceOfElementLocated(acceptFormLocator));
        acceptForm.findElement(By.cssSelector("button[type=submit]")).click();
        driver2.switchTo().alert().accept();

        WebElement acceptBanner = wait(driver2).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(acceptBanner.getText().contains("Đã nhận ca thành công"));
    }

    // AF1: attempt to hand off a shift to oneself. The dropdown never lists the
    // current user, so we inject the option via JavaScript to simulate a
    // tampered request and confirm the SERVER still rejects it.
    @Test
    public void testAlternativeFlow_selfTransferIsRejectedByServer() throws SQLException {
        int shiftId = seedScheduledShift();

        driver1 = newDriver();
        login(driver1, REQUESTER_EMAIL, PASSWORD);
        driver1.get(BASE_URL + "/employee/my-shifts");

        wait(driver1).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#shift-wrap-" + shiftId + " button"))).click();
        WebElement panel = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#handoff-" + shiftId + ".open")));

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

    // AF4: target employee declines the request.
    @Test
    public void testAlternativeFlow_rejectShiftExchange() throws SQLException {
        int shiftId = seedScheduledShift();

        driver1 = newDriver();
        login(driver1, REQUESTER_EMAIL, PASSWORD);
        driver1.get(BASE_URL + "/employee/my-shifts");
        wait(driver1).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#shift-wrap-" + shiftId + " button"))).click();
        WebElement panel = wait(driver1).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#handoff-" + shiftId + ".open")));
        new Select(panel.findElement(By.name("targetEmpId"))).selectByValue(String.valueOf(targetId));
        panel.findElement(By.cssSelector("button[type=submit]")).click();
        wait(driver1).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));

        int requestId = getPendingRequestId(shiftId);

        driver2 = newDriver();
        login(driver2, TARGET_EMAIL, PASSWORD);
        driver2.get(BASE_URL + "/employee/my-shifts");

        By rejectFormLocator = By.xpath(
                "//input[@name='requestId'][@value='" + requestId + "']"
                        + "/ancestor::form[.//input[@name='action'][@value='reject_exchange']]");
        WebElement rejectForm = wait(driver2).until(ExpectedConditions.presenceOfElementLocated(rejectFormLocator));
        rejectForm.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement banner = wait(driver2).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Đã từ chối yêu cầu"));
    }
}
