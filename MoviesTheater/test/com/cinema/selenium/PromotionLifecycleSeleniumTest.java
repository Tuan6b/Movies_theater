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
 * Selenium WebDriver end-to-end tests for the Promotion Lifecycle Management
 * Decision Table (Reactivate / Hard Delete in PromotionServlet). Covers all
 * 6 rules from doc/Huong_dan_Decision_Table_Testing.docx section 5, driven
 * through the real Manager UI (not just the extracted decidePromotionAction
 * unit test).
 *
 * These are real browser tests: they require the app to already be running
 * at BASE_URL (deployed via NetBeans/Tomcat) and a reachable SQL Server
 * database. Browser: Microsoft Edge (Chromium) via EdgeDriver.
 */
public class PromotionLifecycleSeleniumTest {

    private static final String BASE_URL = "http://localhost:8088/MoviesTheater";
    private static final String MANAGER_EMAIL = "manager@cinema.vn";
    private static final String PASSWORD = "123456";
    private static final int ANY_ACCOUNT_ID = 3; // FK owner for seeded Invoice rows

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

    private int seedPromotion(String status, int usedCount) throws SQLException {
        String code = "SELDT" + (System.nanoTime() % 100000000L);
        String sql = "INSERT INTO Promotion (PromotionCode, Description, DiscountType, DiscountValue, "
                + "MinOrderAmount, MaxDiscountAmount, StartDate, EndDate, UsageLimit, UsedCount, IsActive, Status) "
                + "VALUES (?, N'Selenium Decision Table test', 'Percentage', 10, 0, NULL, "
                + "DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, 10, GETDATE()), NULL, ?, 1, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setInt(2, usedCount);
            ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updatePromotionStatus(int promotionId, String newStatus) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Promotion SET Status = ? WHERE PromotionID = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, promotionId);
            ps.executeUpdate();
        }
    }

    private void updatePromotionUsedCount(int promotionId, int newUsedCount) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Promotion SET UsedCount = ? WHERE PromotionID = ?")) {
            ps.setInt(1, newUsedCount);
            ps.setInt(2, promotionId);
            ps.executeUpdate();
        }
    }

    private void seedPaidInvoiceFor(int promotionId) throws SQLException {
        String sql = "INSERT INTO Invoice (AccountID, PromotionID, SubTotal, DiscountAmount, "
                + "TotalAmount, PaymentMethod, PaymentStatus) VALUES (?, ?, 100000, 10000, 90000, 'Cash', 'Paid')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ANY_ACCOUNT_ID);
            ps.setInt(2, promotionId);
            ps.executeUpdate();
        }
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

    private WebElement findActionForm(int promotionId, String actionValue) {
        return driver.findElement(By.xpath(
                "//input[@name='promotionId'][@value='" + promotionId + "']"
                        + "/ancestor::form[.//input[@name='action'][@value='" + actionValue + "']]"));
    }

    private void submitAndAccept(WebElement form) {
        form.findElement(By.cssSelector("button[type=submit]")).click();
        driver.switchTo().alert().accept();
    }

    // R1: Reactivate a promotion whose status is "expired" -> blocked.
    // Loaded while still "inactive" (button visible), flipped to "expired"
    // in the DB before the stale button is clicked - a race-condition style
    // scenario, since the UI never renders a Reactivate button on an
    // already-expired promotion.
    @Test
    public void testReactivate_R1_expiredPromotionIsBlocked() throws SQLException {
        int id = seedPromotion("inactive", 0);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=inactive");
        WebElement form = findActionForm(id, "reactivate");

        updatePromotionStatus(id, "expired");

        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger, .alert-danger, .flash-error")));
        assertTrue(banner.getText().contains("Cannot reactivate an expired promotion"));
    }

    // R2: Reactivate a promotion that is NOT expired (inactive) -> succeeds.
    @Test
    public void testReactivate_R2_notExpiredSucceeds() throws SQLException {
        int id = seedPromotion("inactive", 0);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=inactive");
        WebElement form = findActionForm(id, "reactivate");
        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success, .alert-success, .flash-success")));
        assertTrue(banner.getText().contains("Promotion reactivated"));
    }

    // R3: Hard-delete a promotion referenced by a paid invoice -> blocked,
    // regardless of usedCount/returnTo.
    @Test
    public void testHardDelete_R3_referencedByPaidInvoiceIsBlocked() throws SQLException {
        int id = seedPromotion("inactive", 0);
        seedPaidInvoiceFor(id);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=inactive");
        WebElement form = findActionForm(id, "hardDelete");
        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger, .alert-danger, .flash-error")));
        assertTrue(banner.getText().contains("referenced by paid invoices"));
    }

    // R4: Hard-delete a used ("upcoming") promotion from the "upcoming" tab
    // -> blocked. The Cancel button only renders when usedCount = 0, so we
    // load the page at usedCount = 0 (button visible), then flip usedCount
    // to 1 in the DB before clicking the now-stale button - simulating
    // another booking consuming the promo code between page load and submit.
    @Test
    public void testHardDelete_R4_usedFromUpcomingTabIsBlocked() throws SQLException {
        int id = seedPromotion("upcoming", 0);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=upcoming");
        WebElement form = findActionForm(id, "hardDelete");

        updatePromotionUsedCount(id, 1);

        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-danger, .alert-danger, .flash-error")));
        assertTrue(banner.getText().contains("already been used"));
    }

    // R5: Hard-delete a used promotion from a tab OTHER than "upcoming"
    // (inactive) -> succeeds.
    @Test
    public void testHardDelete_R5_usedFromOtherTabSucceeds() throws SQLException {
        int id = seedPromotion("inactive", 3);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=inactive");
        WebElement form = findActionForm(id, "hardDelete");
        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success, .alert-success, .flash-success")));
        assertTrue(banner.getText().contains("permanently deleted"));
    }

    // R6: Hard-delete a never-used promotion -> succeeds.
    @Test
    public void testHardDelete_R6_neverUsedSucceeds() throws SQLException {
        int id = seedPromotion("inactive", 0);

        driver = newDriver();
        login(driver, MANAGER_EMAIL, PASSWORD);
        driver.get(BASE_URL + "/manager/promotions?view=inactive");
        WebElement form = findActionForm(id, "hardDelete");
        submitAndAccept(form);

        WebElement banner = wait(driver).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cgv-alert-success, .alert-success, .flash-success")));
        assertTrue(banner.getText().contains("permanently deleted"));
    }
}
