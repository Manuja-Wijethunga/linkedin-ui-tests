package com.student.linkedin.pages;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.student.linkedin.utils.ConfigReader;

public class SearchResultsPage extends BasePage {

    /** What was clicked: the link target, the visible name, and whether the page had to be opened by URL. */
    public record ClickedResult(String href, String name, boolean openedDirectly) {
    }

    private static final String[] CLICK_METHODS = {"mouse click", "WebElement click", "JavaScript click"};

    // Links to a person, company or school, excluding the top navigation/header
    // (which contains a link to the logged-in user's own profile).
    private final By resultLinks = By.xpath(
            "//a[contains(@href,'/in/') or contains(@href,'/company/') or contains(@href,'/school/')]"
                    + "[not(ancestor::nav)][not(ancestor::header)]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.urlContains("/search/results"));
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** LinkedIn opens the Posts tab first (its links belong to post authors), so open the People results. */
    public SearchResultsPage openPeopleTab(String term) {
        String url = ConfigReader.get("base.url") + "/search/results/people/?keywords="
                + URLEncoder.encode(term, StandardCharsets.UTF_8);
        System.out.println("[results] Opening the People results: " + url);
        driver.get(url);
        wait.until(ExpectedConditions.urlContains("/search/results/people"));
        return this;
    }

    /** First visible result link; when mustMatch is true its text must contain the term. */
    private WebElement pickLink(WebDriver d, String term, boolean mustMatch) {
        String wanted = term == null ? "" : term.trim().toLowerCase();
        for (WebElement link : d.findElements(resultLinks)) {
            String href = link.getAttribute("href");
            if (!link.isDisplayed() || href == null || href.isEmpty()) {
                continue;
            }
            if (!mustMatch || wanted.isEmpty() || link.getText().toLowerCase().contains(wanted)) {
                return link;
            }
        }
        return null;
    }

    /** Fluent wait: prefers a result matching the term; if none shows up, accepts the first visible result. */
    private WebElement waitForResult(String term) {
        FluentWait<WebDriver> fluent = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        try {
            return fluent.until(d -> pickLink(d, term, true));
        } catch (TimeoutException e) {
            return fluent.until(d -> pickLink(d, null, false));
        }
    }

    public boolean hasVisibleResult() {
        try {
            return waitForResult(null).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String firstLine(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        int newline = trimmed.indexOf('\n');
        return (newline >= 0 ? trimmed.substring(0, newline) : trimmed).trim();
    }

    private String pathOf(String href) {
        try {
            return URLDecoder.decode(URI.create(href).getPath(), StandardCharsets.UTF_8)
                    .toLowerCase().replaceAll("/$", "");
        } catch (IllegalArgumentException e) {
            return href.toLowerCase();
        }
    }

    /** Waits up to 8 s for the browser URL to contain the clicked link's path. */
    private boolean waitForNavigation(String href) {
        String path = pathOf(href);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> d.getCurrentUrl().toLowerCase().contains(path));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** If the click opened a new tab, continue in that tab. */
    private void switchToNewTab(Set<String> handlesBefore) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> d.getWindowHandles().size() > handlesBefore.size());
        } catch (TimeoutException e) {
            return; // no new tab opened
        }
        for (String handle : driver.getWindowHandles()) {
            if (!handlesBefore.contains(handle)) {
                driver.switchTo().window(handle);
                System.out.println("[results] The result opened in a new tab; switched to it");
                return;
            }
        }
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis); // display only, so the chosen result can be seen; not used for synchronisation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Tries each real click in turn; returns the name of the one that opened the result, or null. */
    private String openByClicking(WebElement chosen, String href) {
        Set<String> handlesBefore = driver.getWindowHandles();
        for (int i = 0; i < CLICK_METHODS.length; i++) {
            try {
                if (i == 0) {
                    new Actions(driver).moveToElement(chosen).click().perform();
                } else if (i == 1) {
                    chosen.click();
                } else {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chosen);
                }
            } catch (StaleElementReferenceException e) {
                throw e;
            } catch (WebDriverException e) {
                System.out.println("[results] " + CLICK_METHODS[i] + " failed: " + e.getClass().getSimpleName());
                continue;
            }
            switchToNewTab(handlesBefore);
            if (waitForNavigation(href)) {
                return CLICK_METHODS[i];
            }
            System.out.println("[results] " + CLICK_METHODS[i] + " did not open the result");
        }
        return null;
    }

    /** Clicks the first visible result and returns what was clicked. */
    public ClickedResult clickFirstResult() {
        return clickFirstResult(null);
    }

    /** Clicks the first result whose text contains the term (or the first visible result as a fallback). */
    public ClickedResult clickFirstResult(String term) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement chosen = waitForResult(term);
                String href = chosen.getAttribute("href");
                String name = firstLine(chosen.getText());
                System.out.println("[results] Chosen result: " + name + " -> " + href);

                // Scroll to the result and outline it in red so the chosen result can be seen
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});"
                                + "arguments[0].style.outline = '4px solid red';", chosen);
                pause(2000);

                String method = openByClicking(chosen, href);
                boolean openedDirectly = false;
                if (method == null) {
                    System.out.println("[results] WARNING: no click opened the result; opening its link directly");
                    driver.get(href);
                    openedDirectly = true;
                } else {
                    System.out.println("[results] Opened by: " + method);
                }
                System.out.println("[results] Now at: " + driver.getCurrentUrl());
                return new ClickedResult(href, name, openedDirectly);
            } catch (StaleElementReferenceException e) {
                // LinkedIn redrew the results list; look the element up again
            }
        }
        throw new NoSuchElementException("Could not click the first search result");
    }
}