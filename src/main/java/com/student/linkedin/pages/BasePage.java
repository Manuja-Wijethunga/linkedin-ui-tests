package com.student.linkedin.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    // LinkedIn redraws parts of the page after it loads, which makes a found element "stale".
    // Actions therefore look the element up again and retry a few times.
    private static final int MAX_ATTEMPTS = 4;

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /** Waits for the first VISIBLE element matching the locator (pages can contain hidden duplicates). */
    protected WebElement waitForVisible(By locator) {
        return wait.until(d -> {
            for (WebElement element : d.findElements(locator)) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
            return null;
        });
    }

    /** Waits for the first element matching the locator that is visible and enabled. */
    protected WebElement waitForClickable(By locator) {
        return wait.until(d -> {
            for (WebElement element : d.findElements(locator)) {
                if (element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            }
            return null;
        });
    }

    protected void click(By locator) {
        for (int attempt = 1; ; attempt++) {
            try {
                waitForClickable(locator).click();
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    protected void type(By locator, String text) {
        for (int attempt = 1; ; attempt++) {
            try {
                WebElement element = waitForVisible(locator);
                element.clear();
                element.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    /** Sends a key (for example Enter) to the element, looking it up again if the page redrew it. */
    protected void pressKey(By locator, Keys key) {
        for (int attempt = 1; ; attempt++) {
            try {
                waitForVisible(locator).sendKeys(key);
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    /** True once a visible element matching the locator exists. The wait already checked visibility,
     *  so the element is not asked again (it may have been redrawn in the meantime). */
    protected boolean isDisplayed(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}