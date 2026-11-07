package com.student.linkedin.pages;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.student.linkedin.utils.ConfigReader;

public class FeedPage extends BasePage {

    // LinkedIn's class names and ids are generated and change, so we use structural/stable attributes.
    private final By navBar = By.cssSelector(
            "nav, header, [role='navigation'], [role='banner']");
    private final By searchBox = By.cssSelector(
            "input[placeholder*='Search' i], input[aria-label*='Search' i], "
                    + "input[role='combobox'], input[type='search']");

    public FeedPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return getCurrentUrl().contains("/feed") && isDisplayed(navBar);
    }

    public SearchResultsPage search(String term) {
        System.out.println("[search] Term from config: '" + term + "'");

        System.out.println("[search] Clicking the search box");
        click(searchBox);

        System.out.println("[search] Typing the term");
        type(searchBox, term);

        System.out.println("[search] Pressing Enter");
        pressKey(searchBox, Keys.ENTER);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> d.getCurrentUrl().contains("/search/results"));
        } catch (TimeoutException e) {
            // Safety net: Enter did not open the results page, so open the same search by URL.
            System.out.println("[search] WARNING: Enter did not open the results page; opening the search URL directly");
            String url = ConfigReader.get("base.url") + "/search/results/all/?keywords="
                    + URLEncoder.encode(term, StandardCharsets.UTF_8);
            driver.get(url);
        }

        System.out.println("[search] Now at: " + driver.getCurrentUrl());
        return new SearchResultsPage(driver);
    }
}