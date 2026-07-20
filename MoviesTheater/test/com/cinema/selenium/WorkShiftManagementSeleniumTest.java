package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end tests for UC53: Manage Work Shift.
 * Covers the Basic Flow's Bulk Assign alternative (AF-1), the duplicate-shift
 * guard (Exception E2), and the past-date guard (Exception E1 / BR-43.2)
 * enforced server-side even though the calendar UI never renders a clickable
 * past day.
 *
 * These are real browser tests: they require the app to already be running
 * at BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server
 * database. Each test seeds its own throwaway Employee account so bulk-assign
 * results are deterministic across repeated suite runs (a reused employee
 * would already have every day of a given month occupied from a prior run).
 *
 * Browser: Microsoft Edge (Chromium) via EdgeDriver.
 */
public class WorkShiftManagementSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String MANAGER_EMAIL = "manager@cinema.vn";
    private static final String PASSWORD = "123456";

    private static final String SEED_PASSWORD_HASH =
            "GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL";

    private WebDriver driver;

    private WebDriver newDriver() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-data-dir=" + System.getProperty("java.io.tmpdir")
                + "selenium-edge-profile-" + System.nanoTime());
        return new EdgeDriver(options);
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private void login(WebDriver driver, String email, String password) {
        driver.get(BASE_URL + "/Login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("#loginForm button[type=submit]")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/manager"));
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // A throwaway Employee with zero WorkShift history, so bulk-assign always
    // starts from a clean slate regardless of how many times this suite has run.
    private int seedEmployee() throws SQLException {
        String email = "sel-shift-emp-" + System.nanoTime() + "@cinema.vn";
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus) "
                + "VALUES (?, ?, 3, 0, 'active')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, SEED_PASSWORD_HASH);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                try (PreparedStatement psP = conn.prepareStatement(
                        "INSERT INTO UserProfile (AccountID, FullName) VALUES (?, N'Selenium Shift Employee')")) {
                    psP.setInt(1, id);
                    psP.executeUpdate();
                }
                return id;
            }
        }
    }

    private void seedShift(int empId, LocalDate date, String startTime, String endTime) throws SQLException {
        String sql = "INSERT INTO WorkShift (EmployeeID, ShiftDate, StartTime, EndTime, Status) "
                + "VALUES (?, ?, ?, ?, 'Scheduled')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setTime(3, java.sql.Time.valueOf(startTime + ":00"));
            ps.setTime(4, java.sql.Time.valueOf(endTime + ":00"));
            ps.executeUpdate();
        }
    }

    // AF-1 (Bulk Assign): assigning a whole future month creates one
    // WorkShift per remaining day; repeating the same call finds every day
    // already occupied and reports the "no availability" exception.
    @Test
    public void testBulkAssign_succeedsThenSecondCallFindsNoAvailability() throws SQLException {
        int empId = seedEmployee();
        LocalDate target = LocalDate.now().plusMonths(2).withDayOfMonth(1);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/shifts?shiftType=8H_CHIEU&year=" + target.getYear()
                + "&month=" + target.getMonthValue());

        new Select(driver.findElement(By.name("employeeId"))).selectByValue(String.valueOf(empId));
        driver.findElement(By.cssSelector("form[action$='/manager/shifts'] button[type=submit]")).click();
        driver.switchTo().alert().accept();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Đã tạo"));

        // Second call: every day this employee could take in that month is
        // now already occupied by the first call.
        driver.get(BASE_URL + "/manager/shifts?shiftType=8H_CHIEU&year=" + target.getYear()
                + "&month=" + target.getMonthValue());
        new Select(driver.findElement(By.name("employeeId"))).selectByValue(String.valueOf(empId));
        driver.findElement(By.cssSelector("form[action$='/manager/shifts'] button[type=submit]")).click();
        driver.switchTo().alert().accept();

        WebElement errorBanner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger")));
        assertTrue(errorBanner.getText().contains("đã có ca này hoặc đã qua"));
    }

    // Exception E2: assigning a shift type an employee already has on that
    // exact date is rejected, driven through the page's own add-form (the
    // same one openAddPanel()/submitAddShift() populate on a real day click).
    @Test
    public void testCreateDuplicateShift_isRejectedByServer() throws SQLException {
        int empId = seedEmployee();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        seedShift(empId, tomorrow, "08:00", "14:00"); // matches 6H_SANG

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/shifts?shiftType=6H_SANG&year=" + tomorrow.getYear()
                + "&month=" + tomorrow.getMonthValue());

        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('af-emp').value = arguments[0];"
                        + "document.getElementById('af-date').value = arguments[1];"
                        + "document.getElementById('add-form').submit();",
                String.valueOf(empId), tomorrow.toString());

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger")));
        assertTrue(banner.getText().contains("đã có ca này vào ngày"));
    }

    // Exception E1 / BR-43.2: the calendar never renders an onclick handler
    // for a past day, so this drives the add-form directly to confirm the
    // server rejects a past-dated shift even if the client-side guard is bypassed.
    @Test
    public void testCreatePastDateShift_isRejectedByServer() throws SQLException {
        int empId = seedEmployee();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/shifts?shiftType=6H_SANG&year=" + today.getYear()
                + "&month=" + today.getMonthValue());

        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('af-emp').value = arguments[0];"
                        + "document.getElementById('af-date').value = arguments[1];"
                        + "document.getElementById('add-form').submit();",
                String.valueOf(empId), yesterday.toString());

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger")));
        assertTrue(banner.getText().contains("quá khứ"));
    }
}
