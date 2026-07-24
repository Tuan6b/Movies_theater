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
 * Selenium WebDriver end-to-end tests for UC01: Register (RegisterController).
 * Covers rules R8/R9 of the Register Decision Table in
 * doc/test_template/Huong_dan_Decision_Table_Testing.docx section 6 - the
 * two rules that depend on the real database (duplicate-email lookup and
 * account creation) and therefore cannot be exercised by the pure
 * RegisterControllerValidateInputTest unit test, which only covers R1-R7.
 *
 * Requires the app running at BASE_URL (deployed via NetBeans/Tomcat) and a
 * reachable SQL Server database. Browser: Microsoft Edge (Chromium).
 */
public class RegisterSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";

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

    private int seedAccount(String email) throws SQLException {
        String sql = "INSERT INTO Account (Email, Password, RoleID, IsBlocked) VALUES (?, 'seed-only-hash', 2, 0)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void fillRegisterForm(WebDriver driver, String fullName, String email,
            String password, String confirmPassword, String phoneNumber) {
        driver.get(BASE_URL + "/Register");
        driver.findElement(By.id("fullName")).sendKeys(fullName);
        driver.findElement(By.id("email")).sendKeys(email);
        if (phoneNumber != null) {
            driver.findElement(By.id("phoneNumber")).sendKeys(phoneNumber);
        }
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("confirmPassword")).sendKeys(confirmPassword);
        driver.findElement(By.cssSelector("#registerForm button[type=submit]")).click();
    }

    // R9: all fields valid and email not yet registered -> account is created
    // (RoleID=2 Customer + UserProfile row) and the browser is redirected to
    // the home page with an active session (server-side auto-login).
    @Test
    public void testValidRegistration_R9_createsAccountAndRedirectsHome() throws SQLException {
        String email = "selenium.register." + System.nanoTime() + "@example.com";

        driver = newDriver();
        fillRegisterForm(driver, "Nguyen Van Selenium", email, "matkhau123", "matkhau123", "0912345678");

        wait(driver).until(ExpectedConditions.urlToBe(BASE_URL + "/"));

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT a.RoleID, a.IsBlocked, p.FullName, p.PhoneNumber "
                                + "FROM Account a JOIN UserProfile p ON p.AccountID = a.AccountID "
                                + "WHERE a.Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("expected the new account to be persisted", rs.next());
                assertEquals(2, rs.getInt("RoleID"));
                assertEquals("Nguyen Van Selenium", rs.getString("FullName"));
                assertEquals("0912345678", rs.getString("PhoneNumber"));
            }
        }
    }

    // R8: all fields individually valid, but the email is already registered
    // -> rejected with "Email này đã được đăng ký." and no second account row
    // is created for that email.
    @Test
    public void testDuplicateEmail_R8_showsAlreadyRegisteredError() throws SQLException {
        String email = "selenium.duplicate." + System.nanoTime() + "@example.com";
        seedAccount(email);

        driver = newDriver();
        fillRegisterForm(driver, "Nguyen Van Trung", email, "matkhau123", "matkhau123", null);

        WebElement emailError = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#emailError.show")));
        assertTrue(emailError.getText().contains("Email này đã được đăng ký."));
        assertEquals(BASE_URL + "/Register", driver.getCurrentUrl());

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Account WHERE Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals("duplicate submission must not create a second row", 1, rs.getInt(1));
            }
        }
    }
}
