package com.finki.bnks.project.server.selenium;

import com.finki.bnks.project.server.selenium.model.LoginPage;
import com.finki.bnks.project.server.selenium.model.TotpPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class TotpPageTest {

    private WebDriver driver;

    @BeforeTest
    public void setUp() {
        driver = getDriver();
    }

    @Test
    public void shouldOpen() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());
        loginPage.login("admin", "admin");
        Thread.sleep(3000);
        assertTrue(new TotpPage(driver).isLoaded());
    }

    @Test
    public void shouldRequireCode() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());
        loginPage.login("admin", "admin");
        Thread.sleep(3000);

        TotpPage totpPage = new TotpPage(driver);
        assertTrue(totpPage.isLoaded());

        totpPage.verify();

        String errorMessage = totpPage.getRequiredCodeErrorMessage();
        assertEquals(errorMessage, "Code is mandatory");
    }

    @Test
    public void shouldNotifyThatCodeMustBeExactly6Digits() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());
        loginPage.login("admin", "admin");
        Thread.sleep(3000);

        TotpPage totpPage = new TotpPage(driver);
        assertTrue(totpPage.isLoaded());

        totpPage.insertCode("123");
        totpPage.verify();

        String errorMessage = totpPage.getCodeLengthOfExactly6DigitsErrorMessage();
        assertEquals(errorMessage, "Code must be exactly 6 digits");
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
