package com.student.linkedin.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage extends BasePage {

    // LinkedIn's ids and class names are auto-generated and change, so we use stable attributes instead.
    private final By usernameInput = By.cssSelector(
            "input[autocomplete*='username'], input[type='email']");
    private final By passwordInput = By.cssSelector(
            "input[type='password']");
    private final By signInButton = By.xpath(
            "//button[normalize-space()='Sign in']");

    // The error message has no stable id/class, so it is located by its visible text (plus ARIA alert fallbacks).
    private final By errorMessage = By.xpath(
            "//*[@role='alert'] "
                    + "| //*[contains(normalize-space(text()), 'Wrong email or password')] "
                    + "| //*[contains(normalize-space(text()), 'wrong password')] "
                    + "| //*[contains(normalize-space(text()), 'incorrect')] "
                    + "| //*[contains(normalize-space(text()), \"couldn't find\")]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        waitForVisible(usernameInput);
        return this;
    }

    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(signInButton);
    }

    /** Logs in and waits for the feed. If LinkedIn shows a checkpoint/CAPTCHA, pauses for manual completion. */
    public FeedPage loginSuccessfully(String username, String password) {
        login(username, password);

        wait.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/feed") || url.contains("checkpoint") || url.contains("challenge");
        });

        if (!driver.getCurrentUrl().contains("/feed")) {
            System.out.println(">>> LinkedIn security checkpoint detected. Complete it MANUALLY in the browser (waiting up to 3 minutes)...");
            new WebDriverWait(driver, Duration.ofMinutes(3)).until(ExpectedConditions.urlContains("/feed"));
        }
        return new FeedPage(driver);
    }

    /** Waits for a visible error element that actually contains text, and returns that text. */
    private String findErrorText() {
        return wait.until(d -> {
            for (WebElement element : d.findElements(errorMessage)) {
                if (element.isDisplayed()) {
                    String text = element.getText().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
            return null;
        });
    }

    public boolean isErrorDisplayed() {
        try {
            return !findErrorText().isEmpty();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getErrorText() {
        return findErrorText();
    }
}