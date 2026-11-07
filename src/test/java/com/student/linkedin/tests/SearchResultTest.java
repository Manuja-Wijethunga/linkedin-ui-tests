package com.student.linkedin.tests;

import com.student.linkedin.pages.DetailPage;
import com.student.linkedin.pages.FeedPage;
import com.student.linkedin.pages.SearchResultsPage;
import com.student.linkedin.pages.SearchResultsPage.ClickedResult;
import com.student.linkedin.utils.ConfigReader;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SearchResultTest extends BaseTest {

    @Test(description = "TC04 - Clicking the first result opens the matching page")
    public void clickFirstResult() {
        // TC04 searches for a person (search.person); TC03 (SearchTest) searches for a topic (search.term).
        String person = ConfigReader.get("search.person");

        FeedPage feed = loginWithConfiguredAccount();
        SearchResultsPage results = feed.search(person);
        Assert.assertTrue(results.isLoaded(), "Search results page did not load");

        // LinkedIn opens the Posts tab first, whose links belong to post authors: open the People results.
        results.openPeopleTab(person);

        ClickedResult clicked = results.clickFirstResult(person);

        // If every real click failed, the page was opened by URL, which does not prove the click works.
        Assert.assertFalse(clicked.openedDirectly(),
                "No click opened the result; the page had to be opened by its URL instead");

        String expectedPath = URLDecoder
                .decode(URI.create(clicked.href()).getPath(), StandardCharsets.UTF_8)
                .toLowerCase()
                .replaceAll("/$", "");

        // Wait until the browser has navigated to the clicked result (case-insensitive comparison)
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(d -> d.getCurrentUrl().toLowerCase().contains(expectedPath));

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedPath),
                "Expected URL to contain " + expectedPath + " but was " + driver.getCurrentUrl());

        // The page heading must match the name of the result that was clicked.
        String heading = new DetailPage(driver).getHeading(clicked.name());
        Assert.assertFalse(heading.isEmpty(), "Page heading was empty");

        if (!clicked.name().isEmpty()) {
            Assert.assertTrue(heading.toLowerCase().contains(clicked.name().toLowerCase()),
                    "Heading '" + heading + "' does not match the clicked result '" + clicked.name() + "'");
        }
    }
}