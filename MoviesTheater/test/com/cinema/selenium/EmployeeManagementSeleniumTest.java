package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * Selenium WebDriver end-to-end tests for Employee Management
 * (UC44: Manage Employee, UC45: View Employee List).
 *
 * These are real browser tests: they require the app to already be running
 * at BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server
 * database seeded from cinema_booking_database (1).sql.
 *
 * Browser: Microsoft Edge (Chromium) via EdgeDriver.
 */
public class EmployeeManagementSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String MANAGER_EMAIL = "manager@cinema.vn";
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

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // Seeds an Employee (RoleID 3) account directly, bypassing the UI, so the
    // toggle test doesn't depend on the "create" test having run first.
    private int seedEmployee(String email) throws SQLException {
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
                        "INSERT INTO UserProfile (AccountID, FullName) VALUES (?, N'Selenium Toggle Employee')")) {
                    psP.setInt(1, id);
                    psP.executeUpdate();
                }
                return id;
            }
        }
    }

    // UC44 (Create) + UC45 (View List): adding a new employee shows the
    // auto-generated temporary password (BR-42.1/BR-42.2 precondition) and
    // the new account then appears in the searchable employee list.
    @Test
    public void testAddEmployee_showsTempPasswordAndAppearsInList() throws SQLException {
        String email = "sel-emp-" + System.nanoTime() + "@cinema.vn";

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/employees?action=add");

        driver.findElement(By.name("fullName")).sendKeys("Selenium New Employee");
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Mật khẩu tạm thời"));

        driver.get(BASE_URL + "/manager/employees?keyword=" + encode(email));
        WebElement row = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[normalize-space()='" + email + "']")));
        assertTrue(row.isDisplayed());
    }

    // UC44 (Deactivate/Activate): toggling status flips the badge shown by UC45's list.
    @Test
    public void testDeactivateThenActivateEmployee() throws SQLException {
        String email = "sel-emp-toggle-" + System.nanoTime() + "@cinema.vn";
        int id = seedEmployee(email);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/employees?keyword=" + encode(email));

        WebElement deactivateForm = driver.findElement(By.xpath(
                "//input[@name='accountId'][@value='" + id + "']"
                        + "/ancestor::form[.//input[@name='blocked'][@value='true']]"));
        deactivateForm.findElement(By.cssSelector("button[type=submit]")).click();
        driver.switchTo().alert().accept();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("deactivated"));

        driver.get(BASE_URL + "/manager/employees?keyword=" + encode(email));
        WebElement badge = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-badge.danger")));
        assertTrue(badge.getText().contains("Inactive"));

        WebElement activateForm = driver.findElement(By.xpath(
                "//input[@name='accountId'][@value='" + id + "']"
                        + "/ancestor::form[.//input[@name='blocked'][@value='false']]"));
        activateForm.findElement(By.cssSelector("button[type=submit]")).click();
        driver.switchTo().alert().accept();

        WebElement banner2 = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner2.getText().contains("Employee activated"));
    }
}
