package com.cinema.selenium;

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
 * Selenium smoke test for UC49: View Ticket Revenue Statistics. There is no
 * dedicated reports servlet - the revenue dashboard is rendered by
 * ManagerServlet.showDashboard(), the same handler behind the Manager's home
 * page, so this simply confirms the aggregated data renders without error
 * for the Manager role.
 *
 * Requires the app running at BASE_URL (deployed via NetBeans/Tomcat) and a
 * reachable SQL Server database. Browser: Microsoft Edge (Chromium).
 */
public class RevenueDashboardSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String MANAGER_EMAIL = "manager@cinema.vn";
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

    @Test
    public void testManagerDashboard_rendersRevenueChartAndKpis() {
        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);

        wait(driver).until(ExpectedConditions.presenceOfElementLocated(By.id("revenueChart")));

        WebElement kpi = driver.findElement(By.cssSelector(".rev-kpi-val"));
        assertFalse(kpi.getText().trim().isEmpty());
        assertTrue(kpi.getText().matches("[\\d.,]+"));
    }
}
