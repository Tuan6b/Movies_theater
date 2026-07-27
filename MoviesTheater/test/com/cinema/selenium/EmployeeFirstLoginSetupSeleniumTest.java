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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Selenium WebDriver end-to-end test for UC52: Employee First-Login Setup.
 * Seeds an Employee account the way EmployeeDAO.add() leaves one after UC44
 * (AccountStatus = 'pending', i.e. Account.NeedsSetup = true per BR-42.1) and
 * drives the mandatory setup gate through a real login.
 *
 * Requires the app running at BASE_URL (deployed via NetBeans/Tomcat) and a
 * reachable SQL Server database. Browser: Microsoft Edge (Chromium).
 */
public class EmployeeFirstLoginSetupSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String PASSWORD = "123456";

    // Base64(salt || SHA-256(salt || "123456")) - same stored hash the seed
    // script (cinema_booking_database (1).sql) uses for every sample account.
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

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private static final String SEED_FULL_NAME = "Selenium First Login Employee";
    private static final String SEED_PHONE     = "0911222333";
    private static final String SEED_ADDRESS   = "12 Selenium Street, District 1";
    private static final String SEED_DOB       = "1999-08-15";

    // Seeds a brand-new Employee with AccountStatus = 'pending' and a profile
    // filled in, exactly as EmployeeDAO.add() leaves an account the Manager just
    // created at UC44 (BR-42.1).
    private String seedPendingEmployee() throws SQLException {
        String email = "sel-firstlogin-" + System.nanoTime() + "@cinema.vn";
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus) "
                + "VALUES (?, ?, 3, 0, 'pending')";
        String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber, Address, DoB) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, SEED_PASSWORD_HASH);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int accountId = keys.getInt(1);
                try (PreparedStatement psP = conn.prepareStatement(sqlProfile)) {
                    psP.setInt(1, accountId);
                    psP.setNString(2, SEED_FULL_NAME);
                    psP.setString(3, SEED_PHONE);
                    psP.setNString(4, SEED_ADDRESS);
                    psP.setDate(5, java.sql.Date.valueOf(SEED_DOB));
                    psP.executeUpdate();
                }
            }
        }
        return email;
    }

    private String accountStatus(String email) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT AccountStatus FROM Account WHERE Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private String profileColumn(String email, String column) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT u." + column + " FROM UserProfile u "
                        + "JOIN Account a ON u.AccountID = a.AccountID WHERE a.Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    @Test
    public void testFirstLogin_forcesSetupThenUnlocksDashboard() throws SQLException {
        String email = seedPendingEmployee();
        assertEquals("pending", accountStatus(email));

        driver = newDriver();
        driver.get(BASE_URL + "/Login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("#loginForm button[type=submit]")).click();

        wait(driver).until(ExpectedConditions.urlContains("/employee/setup"));

        // Guard: trying to jump straight to the dashboard bounces back to setup.
        driver.get(BASE_URL + "/employee/dashboard");
        wait(driver).until(ExpectedConditions.urlContains("/employee/setup"));

        // Setup only replaces the temporary password now: the profile the Manager
        // entered is shown read-only, so there is nothing to retype.
        assertTrue("Setup must not ask again for details the Manager already entered",
                driver.findElements(By.name("phoneNumber")).isEmpty()
                        && driver.findElements(By.name("dateOfBirth")).isEmpty()
                        && driver.findElements(By.name("address")).isEmpty()
                        && driver.findElements(By.name("fullName")).isEmpty());
        assertTrue(driver.getPageSource().contains(SEED_ADDRESS));

        driver.findElement(By.name("newPassword")).sendKeys("newpass123");
        driver.findElement(By.cssSelector("form button[type=submit]")).click();

        // dashboard.jsp does not render the flashSuccess banner, so the strongest
        // UI-visible proof of success is landing on the dashboard with the header
        // showing the name the Manager gave the account.
        wait(driver).until(ExpectedConditions.urlMatches(".*/employee/?$"));
        WebElement userName = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-user-name")));
        assertTrue(userName.getText().contains(SEED_FULL_NAME));

        assertEquals("active", accountStatus(email));

        // The regression this screen used to carry: posting setup wiped whatever the
        // Manager had entered, because the form could not resend address or DoB.
        assertEquals(SEED_FULL_NAME, profileColumn(email, "FullName"));
        assertEquals(SEED_PHONE,     profileColumn(email, "PhoneNumber"));
        assertEquals(SEED_ADDRESS,   profileColumn(email, "Address"));
        assertEquals(SEED_DOB,       profileColumn(email, "DoB"));
    }
}
