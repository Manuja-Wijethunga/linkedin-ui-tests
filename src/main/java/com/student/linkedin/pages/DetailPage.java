package com.student.linkedin.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DetailPage extends BasePage {

    // Headings in the page body. Headings inside the top bar (such as the notifications
    // panel's "0 notifications") are excluded because they are not the page's own heading.
    private final By pageHeadings = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][not(ancestor::nav)][not(ancestor::header)]");

    public DetailPage(WebDriver driver) {
        super(driver);
    }

    public String getHeading() {
        return getHeading(null);
    }

    /**
     * Waits for a visible body heading that contains the expected name and returns its text.
     * If none appears, falls back to the first visible body heading, then to the tab title.
     */
    public String getHeading(String expectedName) {
        boolean hasName = expectedName != null && !expectedName.trim().isEmpty();
        String wanted = hasName ? expectedName.trim().toLowerCase() : "";

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(15))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        for (WebElement heading : d.findElements(pageHeadings)) {
                            if (!heading.isDisplayed()) {
                                continue;
                            }
                            String text = heading.getText().trim();
                            if (text.isEmpty()) {
                                continue;
                            }
                            if (!hasName || text.toLowerCase().contains(wanted)) {
                                return text;
                            }
                        }
                        return null;
                    });
        } catch (TimeoutException e) {
            System.out.println("[detail] No heading contained '" + expectedName + "'; using a fallback");
            for (WebElement heading : driver.findElements(pageHeadings)) {
                try {
                    if (heading.isDisplayed() && !heading.getText().trim().isEmpty()) {
                        return heading.getText().trim();
                    }
                } catch (StaleElementReferenceException ignored) {
                    // the page was redrawn; try the next one
                }
            }
            String title = driver.getTitle();
            if (title == null) {
                return "";
            }
            return title.replaceAll("^\\(\\d+\\)\\s*", "")
                    .replaceAll("\\s*\\|\\s*LinkedIn.*$", "")
                    .trim();
        }
    }
}