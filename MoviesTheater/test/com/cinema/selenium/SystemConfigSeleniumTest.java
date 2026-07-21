package com.cinema.selenium;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Selenium WebDriver end-to-end test for UC50: Manage System Configuration.
 * Confirms a saved value both round-trips through the SystemConfig upsert
 * and, per BR-23, generates a CONFIG_UPDATE row that UC51 (View System Logs)
 * can later display.
 *
 * Requires the app running at BASE_URL (deployed via NetBeans/Tomcat) and a
 * reachable SQL Server database. Browser: Microsoft Edge (Chromium).
 */
public class SystemConfigSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String ADMIN_EMAIL = "admin@cinema.vn";
    private static final String PASSWORD = "123456";

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
        // Admin, like Manager, lands on /manager after login (LoginController
        // routes roleId 5 and 4 to the same dashboard entry point).
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/manager"));
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void testUpdateCinemaPhone_persistsAndWritesAuditLog() throws SQLException {
        String newPhone = "0900-" + (System.nanoTime() % 1000000L);

        driver = newDriver();
        login(driver, ADMIN_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/admin/config");

        WebElement phoneField = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("cinema_phone")));
        phoneField.clear();
        phoneField.sendKeys(newPhone);
        driver.findElement(By.cssSelector("form button[type=submit]")).click();

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success")));
        assertTrue(banner.getText().contains("Cấu hình hệ thống đã được lưu"));

        driver.get(BASE_URL + "/admin/config");
        WebElement reloadedField = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("cinema_phone")));
        assertEquals(newPhone, reloadedField.getAttribute("value"));

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT TOP 1 ActionType, Description FROM SystemLog "
                                + "WHERE ActionType = 'CONFIG_UPDATE' ORDER BY LogID DESC");
                ResultSet rs = ps.executeQuery()) {
            assertTrue("expected a CONFIG_UPDATE audit row (BR-23)", rs.next());
            assertEquals("CONFIG_UPDATE", rs.getString("ActionType"));
        }
    }
}
