package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end tests for UC46: Ticket Check-in.
 * Per the SRS's own correction, there is no QR/barcode scanner anywhere in
 * the codebase - check-in is a manual text-code lookup followed by a
 * confirm click (BR-46.1), which is exactly what these tests drive.
 *
 * These are real browser tests: they require the app to already be running
 * at BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server
 * database. Each test seeds its own throwaway Room/Seat/Schedule/Invoice/
 * Ticket chain so it does not depend on the exact contents of
 * cinema_booking_database (1).sql.
 *
 * Browser: Microsoft Edge (Chromium) via EdgeDriver.
 */
public class TicketCheckinSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String EMPLOYEE_EMAIL = "employee@cinema.vn";
    private static final String PASSWORD = "123456";
    private static final int ANY_ACCOUNT_ID = 3; // FK owner for seeded Invoice rows (employee@cinema.vn)

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
                .until(ExpectedConditions.urlContains("/employee"));
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Seeds a fully paid, bookable ticket (own Room/Seat/Schedule/Invoice) and
    // returns its unique text Code, ready to be looked up on /employee/checkin.
    private String seedPaidTicket(boolean checkedIn) throws SQLException {
        try (Connection conn = DBUtils.getConnection()) {
            int movieId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 1 MovieID FROM Movie WHERE IsActive = 1");
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                movieId = rs.getInt(1);
            }

            int roomId;
            String roomNumber = "SELCHK-" + (System.nanoTime() % 100000000L);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Room (RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow) "
                            + "VALUES (?, '2D', 10, 1, 10)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, roomNumber);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    roomId = keys.getInt(1);
                }
            }

            int seatId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (?, 'A', 1, 'Normal')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    seatId = keys.getInt(1);
                }
            }

            int scheduleId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status) "
                            + "VALUES (?, ?, DATEADD(DAY, 1, GETDATE()), DATEADD(DAY, 1, DATEADD(HOUR, 2, GETDATE())), 90000, 'Scheduled')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, roomId);
                ps.setInt(2, movieId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    scheduleId = keys.getInt(1);
                }
            }

            int invoiceId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) "
                            + "VALUES (?, 90000, 0, 90000, 'Cash', 'Paid')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ANY_ACCOUNT_ID);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    invoiceId = keys.getInt(1);
                }
            }

            String code = "SELCHK" + System.nanoTime();
            String sqlTicket = "INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) "
                    + "VALUES (?, ?, ?, 90000, ?, ?, " + (checkedIn ? "GETDATE()" : "NULL") + ")";
            try (PreparedStatement ps = conn.prepareStatement(sqlTicket)) {
                ps.setInt(1, scheduleId);
                ps.setInt(2, seatId);
                ps.setInt(3, invoiceId);
                ps.setString(4, code);
                ps.setBoolean(5, checkedIn);
                ps.executeUpdate();
            }
            return code;
        }
    }

    // Basic Flow: Employee looks up a not-yet-checked-in ticket by its text
    // code, confirms check-in, and re-searching shows the updated status.
    @Test
    public void testCheckinByCode_marksTicketAsCheckedIn() throws SQLException {
        String code = seedPaidTicket(false);

        driver = newDriver();
        login(driver, EMPLOYEE_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/employee/checkin?code=" + code);

        WebElement result = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkin-result")));
        assertTrue(result.getText().contains("CHƯA CHECK-IN"));

        result.findElement(By.cssSelector("form button[type=submit]")).click();

        wait(driver).until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".cgv-alert-success")));

        driver.get(BASE_URL + "/employee/checkin?code=" + code);
        WebElement resultAfter = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkin-result")));
        assertTrue(resultAfter.getText().contains("ĐÃ CHECK-IN"));
        assertTrue(resultAfter.findElements(By.cssSelector("form")).isEmpty());
    }

    // A ticket that is already checked in shows its status with no confirm
    // button available (no double check-in path through the UI).
    @Test
    public void testAlreadyCheckedInTicket_showsNoConfirmButton() throws SQLException {
        String code = seedPaidTicket(true);

        driver = newDriver();
        login(driver, EMPLOYEE_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/employee/checkin?code=" + code);

        WebElement result = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkin-result")));
        assertTrue(result.getText().contains("ĐÃ CHECK-IN"));
        assertFalse(result.getText().contains("CHƯA CHECK-IN"));
        assertTrue(result.findElements(By.cssSelector("form")).isEmpty());
    }
}
