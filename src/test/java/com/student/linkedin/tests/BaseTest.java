package com.student.linkedin.tests;

import java.time.Duration;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.student.linkedin.pages.FeedPage;
import com.student.linkedin.pages.LoginPage;
import com.student.linkedin.utils.ConfigReader;
import com.student.linkedin.utils.ScreenshotUtil;

public class BaseTest {

    // How long the final page stays on screen after each test (for the screen recording).
    private static final int DEFAULT_RESULT_PAUSE_SECONDS = 12;

    protected WebDriver driver;
    protected String baseUrl;
    protected String username;
    protected String password;

    @BeforeMethod
    public void setUp() {
        baseUrl = ConfigReader.get("base.url");
        username = ConfigReader.get("linkedin.username");
        password = ConfigReader.get("linkedin.password");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        // Return from driver.get() once the page structure is ready, not after every image/script loads.
        // The page classes use explicit waits for the elements they need.
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        driver = new ChromeDriver(options);
        // Only explicit waits are used (in the page classes); no implicit wait, to avoid mixing them.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null) {
            // Take the screenshot BEFORE the browser is closed
            String status = result.isSuccess() ? "PASSED" : "FAILED";
            String path = ScreenshotUtil.capture(driver, result.getName() + "_" + status);
            System.out.println("Screenshot saved: " + path);

            showResult();
            driver.quit();
        }
    }

    /** Keeps the final page visible so the result can be seen (and recorded). Not used for synchronisation. */
    private void showResult() {
        int seconds = DEFAULT_RESULT_PAUSE_SECONDS;
        String configured = ConfigReader.get("result.pause.seconds");
        if (configured != null && !configured.trim().isEmpty()) {
            try {
                seconds = Integer.parseInt(configured.trim());
            } catch (NumberFormatException ignored) {
                // keep the default
            }
        }
        if (seconds <= 0) {
            return;
        }
        System.out.println("Showing the result for " + seconds + " seconds...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected FeedPage loginWithConfiguredAccount() {
        return new LoginPage(driver).open(baseUrl).loginSuccessfully(username, password);
    }
}