package com.student.linkedin.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.student.linkedin.pages.FeedPage;
import com.student.linkedin.pages.SearchResultsPage;
import com.student.linkedin.utils.ConfigReader;

public class SearchTest extends BaseTest {

    @Test(description = "TC03 - Search shows a results page")
    public void searchShowsResults() {
        FeedPage feed = loginWithConfiguredAccount();
        SearchResultsPage results = feed.search(ConfigReader.get("search.term"));

        Assert.assertTrue(results.isLoaded(), "URL does not look like a search results page: " + results.getCurrentUrl());
        Assert.assertTrue(results.hasVisibleResult(), "No visible search result element found");
    }
}