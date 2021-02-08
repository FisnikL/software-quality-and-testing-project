package com.finki.bnks.project.server.selenium;

import com.finki.bnks.project.server.selenium.model.LoginPage;
import com.finki.bnks.project.server.selenium.model.SignInSuccessfulPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class LoginPageTest {

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
    }

    @Test
    public void shouldLoginWithoutTotp() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());
        loginPage.login("lazy", "lazy");
        Thread.sleep(3000);
        assertTrue(new SignInSuccessfulPage(driver).isLoaded());

        loginPage.logout();
        assertTrue(loginPage.isLoaded());
    }

    @Test
    public void shouldRequireUsernameAndPassword() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());
        loginPage.login("", "");
        String requiredUsernameErrorMessage = loginPage.getRequiredUsernameErrorMessage();
        String requiredPasswordErrorMessage = loginPage.getRequiredPasswordErrorMessage();
        assertEquals(requiredUsernameErrorMessage, "Username is mandatory");
        assertEquals(requiredPasswordErrorMessage, "Password is mandatory");
    }

    @Test
    public void shouldNotBeAllowedToLoginWithInvalidUsername() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());

        loginPage.login("fisnikl", "lazy");
        Thread.sleep(1000);
        Boolean failed = loginPage.getLoginFailedErrorResult();
        assertTrue(failed);
    }

    @Test
    public void shouldNotBeAllowedToLoginWithInvalidPassword() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());

        loginPage.login("lazy", "fisnikl");
        Thread.sleep(2000);
        Boolean failed = loginPage.getLoginFailedErrorResult();
        assertTrue(failed);
    }

    @Test
    public void shouldNotBeAllowedToLoginWithEmptyUsername() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());

        loginPage.login("", "FisnikL123");
        String errorMessage = loginPage.getRequiredUsernameErrorMessage();

        assertEquals("Username is mandatory", errorMessage);
    }

    @Test
    public void shouildNotBeAllowedLoginWithEmptyPassword() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        assertTrue(loginPage.isLoaded());

        loginPage.login("fisnikl", "");
        String errorMessage = loginPage.getRequiredPasswordErrorMessage();
        assertEquals(errorMessage, "Password is mandatory");
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
