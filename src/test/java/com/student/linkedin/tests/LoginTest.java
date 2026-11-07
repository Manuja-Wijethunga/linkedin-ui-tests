package com.student.linkedin.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.student.linkedin.pages.FeedPage;
import com.student.linkedin.pages.LoginPage;

public class LoginTest extends BaseTest {

    @Test(description = "TC01 - Login with valid credentials")
    public void validLogin() {
        FeedPage feed = new LoginPage(driver).open(baseUrl).loginSuccessfully(username, password);
        Assert.assertTrue(feed.isLoaded(), "Feed page / navigation bar was not displayed after login");
    }

    @Test(description = "TC02 - Login with invalid password shows error")
    public void invalidPasswordLogin() {
        LoginPage login = new LoginPage(driver).open(baseUrl);
        login.login(username, "WrongPassword!123");
        Assert.assertTrue(login.isErrorDisplayed(), "Error message was not displayed");
        Assert.assertFalse(login.getErrorText().isEmpty(), "Error message was empty");
    }
}