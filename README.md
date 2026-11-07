# LinkedIn UI Test Suite (IT3153, Assignment 05)

Automated UI tests for LinkedIn using Java 17, Selenium WebDriver, TestNG and Maven (Page Object Model):
valid login, invalid login, search, and opening a search result.

## Prerequisites
- Java 17 or later
- Maven 3.8 or later
- Google Chrome (ChromeDriver is downloaded automatically by Selenium Manager)

## Configure credentials
1. Copy `config.properties.example` to `config.properties`.
2. Fill in your own LinkedIn account details:

```
linkedin.username=YOUR_EMAIL
linkedin.password=YOUR_PASSWORD
base.url=https://www.linkedin.com
search.term=system testing
search.person=Satya Nadella
```

`config.properties` is listed in `.gitignore` and must never be committed.
The environment variables `LINKEDIN_USERNAME` and `LINKEDIN_PASSWORD` can be used instead.

Optional: `result.pause.seconds=12` sets how long each test keeps its final page on screen (default 12, use 0 to skip).

## Run the tests
All tests:

    mvn test

One test (PowerShell):

    mvn test "-Dtest=LoginTest#validLogin"
    mvn test "-Dtest=LoginTest#invalidPasswordLogin"
    mvn test "-Dtest=SearchTest"
    mvn test "-Dtest=SearchResultTest"

## Output
- Screenshots of every test: `screenshots/`
- Maven reports: `target/surefire-reports/`

## Notes
- If LinkedIn shows a CAPTCHA or security check, the test waits up to 3 minutes. Complete it by hand in the browser. The suite never tries to bypass it.
- LinkedIn changes its page structure often, so locators are defined at the top of each page class.
- Use only your own account. LinkedIn's User Agreement restricts automated access, so keep the number of runs small.