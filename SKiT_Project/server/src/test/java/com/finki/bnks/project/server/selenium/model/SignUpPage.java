package com.finki.bnks.project.server.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SignUpPage extends BasePage {

    public SignUpPage(WebDriver driver){
        super(driver);
    }

    public void open() throws InterruptedException {
        new LoginPage(driver).open();
        Thread.sleep(3000);
        driver.findElement(By.id("sign-up-button")).click();
    }

    public boolean isLoaded() throws InterruptedException {
        Thread.sleep(5000);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sign-up"))).isDisplayed();
    }

    public void signup(String user, String password) throws InterruptedException {
        // Login id: using xpath
        driver.findElement(By.xpath("//input[@id='username']")).clear();
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys(user);
        Thread.sleep(5000);

        driver.findElement(By.xpath("//input[@id='password']")).clear();
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
        Thread.sleep(5000);

        driver.findElement(By.id("sign-up-button")).click();

    }

    public String getRequiredUsernameErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("username-input-error"));
        return errorPage.getText();
    }

    public String getRequiredPasswordErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("password-input-error"));
        return errorPage.getText();
    }

    public String getPasswordMinimumLengthOf8CharactersMessage() {
        WebElement errorPage = driver.findElement(By.id("password-input-error"));
        return errorPage.getText();
    }
}
