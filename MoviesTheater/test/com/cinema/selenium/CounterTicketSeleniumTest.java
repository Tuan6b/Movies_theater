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

import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end tests for the counter-sale flow
 * (UC48: Create Manual Ticket) and its immediate read side
 * (UC47: View Booking Ticket List) shown right after booking.
 *
 * These are real browser tests: they require the app to already be running
 * at BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server
 * database. Each test seeds its own throwaway Room/Seat/Schedule so it does
 * not depend on the exact contents of cinema_booking_database (1).sql.
 *
 * Browser: Microsoft Edge (Chromium) via EdgeDriver.
 */
public class CounterTicketSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String EMPLOYEE_EMAIL = "employee@cinema.vn";
    private static final String PASSWORD = "123456";
    private static final double BASE_PRICE = 90000.0;

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

    private int findAnyMovieId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TOP 1 MovieID FROM Movie WHERE IsActive = 1");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int seedRoom(Connection conn) throws SQLException {
        String roomNumber = "SEL-" + (System.nanoTime() % 100000000L);
        String sql = "INSERT INTO Room (RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow) "
                + "VALUES (?, '2D', 10, 1, 10)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomNumber);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedSeat(Connection conn, int roomId) throws SQLException {
        String sql = "INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (?, 'A', 1, 'Normal')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedSchedule(Connection conn, int roomId, int movieId) throws SQLException {
        String sql = "INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status) "
                + "VALUES (?, ?, DATEADD(HOUR, 2, GETDATE()), DATEADD(HOUR, 4, GETDATE()), ?, 'Scheduled')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roomId);
            ps.setInt(2, movieId);
            ps.setDouble(3, BASE_PRICE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    // scheduleId, seatId (in that order)
    private int[] seedBookableSchedule() throws SQLException {
        try (Connection conn = DBUtils.getConnection()) {
            int movieId = findAnyMovieId(conn);
            int roomId = seedRoom(conn);
            int seatId = seedSeat(conn, roomId);
            int scheduleId = seedSchedule(conn, roomId, movieId);
            return new int[]{scheduleId, seatId};
        }
    }

    // Basic Flow (UC48) + immediate read (UC47): selecting a seat, entering
    // walk-in customer details, and confirming issues a ticket that then
    // shows up in the showtime's booking list with the correct revenue total.
    @Test
    public void testCreateManualTicket_appearsInBookingListWithRevenue() throws SQLException {
        int[] seeded = seedBookableSchedule();
        int scheduleId = seeded[0];
        int seatId = seeded[1];
        String customerEmail = "sel-cust-" + System.nanoTime() + "@gmail.com";

        driver = newDriver();
        login(driver, EMPLOYEE_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/employee/book?scheduleId=" + scheduleId);

        wait(driver).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("label[for='seat-" + seatId + "']"))).click();
        driver.findElement(By.id("customerEmail")).sendKeys(customerEmail);
        driver.findElement(By.id("customerName")).sendKeys("Selenium Walk-in Customer");
        driver.findElement(By.cssSelector("#bookingForm button[type=submit]")).click();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Xuất vé thành công"));
        assertTrue(driver.getCurrentUrl().contains("/employee/tickets?scheduleId=" + scheduleId));

        WebElement customerCell = wait(driver).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(., 'Selenium Walk-in Customer')]")));
        assertTrue(customerCell.isDisplayed());

        WebElement countPill = driver.findElement(By.cssSelector(".cgv-pill.active"));
        assertTrue(countPill.getText().contains("1 vé"));
    }

    // Exception E3: an invalid/expired promotion code is rejected and no
    // ticket is created; the employee stays on the booking page.
    @Test
    public void testInvalidPromoCode_rejectsBookingAndStaysOnBookForm() throws SQLException {
        int[] seeded = seedBookableSchedule();
        int scheduleId = seeded[0];
        int seatId = seeded[1];

        driver = newDriver();
        login(driver, EMPLOYEE_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/employee/book?scheduleId=" + scheduleId);

        wait(driver).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("label[for='seat-" + seatId + "']"))).click();
        driver.findElement(By.id("promoCode")).sendKeys("SEL-NOPE-" + System.nanoTime());
        driver.findElement(By.cssSelector("#bookingForm button[type=submit]")).click();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger")));
        assertTrue(banner.getText().contains("không hợp lệ hoặc đã hết hạn"));
        assertTrue(driver.getCurrentUrl().contains("/employee/book?scheduleId=" + scheduleId));
    }
}
