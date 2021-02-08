package com.finki.bnks.project.server.selenium;

import com.finki.bnks.project.server.selenium.model.SignUpPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class SignUpPageTest {

    private WebDriver driver;

    @BeforeTest
    public void setUp() {
        driver = getDriver();
    }

    @Test
    public void shouldOpen() throws InterruptedException {
        SignUpPage signUpPage = new SignUpPage(driver);
        signUpPage.open();
        assertTrue(signUpPage.isLoaded());
    }

    @Test
    public void shouldRequireUsernameAndPassword() throws InterruptedException {
        SignUpPage signUpPage = new SignUpPage(driver);
        signUpPage.open();
        assertTrue(signUpPage.isLoaded());

        signUpPage.signup("", "");

        String requiredUsernameErrorMessage = signUpPage.getRequiredUsernameErrorMessage();
        String requiredPasswordErrorMessage = signUpPage.getRequiredPasswordErrorMessage();
        assertEquals(requiredUsernameErrorMessage, "Username is mandatory");
        assertEquals(requiredPasswordErrorMessage, "Password is mandatory");
    }

    @Test
    public void ShouldRequireMinimumLengthOfCharactersForPassword() throws InterruptedException {
        SignUpPage signUpPage = new SignUpPage(driver);
        signUpPage.open();
        assertTrue(signUpPage.isLoaded());

        signUpPage.signup("fisnikl", "1234");

        String passwordMinimumOf8CharactersErrorMessage = signUpPage.getPasswordMinimumLengthOf8CharactersMessage();
        assertEquals(passwordMinimumOf8CharactersErrorMessage, "Minimum length is 8 characters");
    }

    private WebDriver getDriver() {
        System.setProperty("webdriver.chrome.driver", "src/test/java/com/finki/bnks/project/server/selenium/driver/chromedriver");
        return new ChromeDriver();
    }

    @AfterTest
    public void tearDown(){
        driver.quit();
    }
}
