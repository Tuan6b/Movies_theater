package com.cinema.selenium;

import com.cinema.util.SystemLogService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * Selenium WebDriver end-to-end tests for UC51: View System Logs.
 * Seeds a uniquely-named log entry through the real SystemLogService (the
 * same utility every other UC in this system calls to audit itself) and
 * confirms both filter mechanisms Normal Flow steps 5-6 describe: filtering
 * by exact action type, and free-text search over the description.
 *
 * Requires the app running at BASE_URL (deployed via NetBeans/Tomcat) and a
 * reachable SQL Server database. Browser: Microsoft Edge (Chromium).
 */
public class SystemLogsSeleniumTest {

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
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/manager"));
    }

    private WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Test
    public void testFilterByActionType_showsSeededLogEntry() {
        String actionType = "SELENIUM_TEST_" + System.nanoTime();
        String description = "Selenium action-type filter check " + System.nanoTime();
        SystemLogService.log(null, actionType, description, "127.0.0.1");

        driver = newDriver();
        login(driver, ADMIN_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/admin/logs?action=" + encode(actionType));

        WebElement badge = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".badge-action")));
        assertTrue(badge.getText().trim().equals(actionType));

        WebElement descCell = driver.findElement(By.cssSelector(".log-desc"));
        assertTrue(descCell.getText().contains(description));
    }

    @Test
    public void testSearchByDescriptionKeyword_showsMatchingLogEntry() {
        String keyword = "SelKw" + System.nanoTime();
        String description = "Selenium keyword search check containing " + keyword;
        SystemLogService.log(null, "SELENIUM_TEST_KEYWORD", description, "127.0.0.1");

        driver = newDriver();
        login(driver, ADMIN_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/admin/logs?q=" + encode(keyword));

        WebElement descCell = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".log-desc")));
        assertTrue(descCell.getText().contains(keyword));
    }
}
