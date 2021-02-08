package com.finki.bnks.project.server.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver){
        super(driver);
    }

    public void open(){
        driver.get("http://localhost:4200/");
    }

    public boolean isLoaded() throws InterruptedException {
        Thread.sleep(5000);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title"))).isDisplayed();
    }

    public void login(String user, String password) throws InterruptedException {
        // Login id: using xpath
        driver.findElement(By.xpath("//input[@id='username']")).clear();
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys(user);
        Thread.sleep(5000);

        driver.findElement(By.xpath("//input[@id='password']")).clear();
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
        Thread.sleep(5000);

        driver.findElement(By.id("sign-in-button")).click();
    }

    public void logout() {
        driver.findElement(By.id("sign-out")).click();
    }

    public String getRequiredUsernameErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("username-input-error"));
        return errorPage.getText();
    }

    public String getRequiredPasswordErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("password-input-error"));
        return errorPage.getText();
    }

    public Boolean getLoginFailedErrorResult() {
        WebElement errorDialog = driver.findElements(By.className("ui-toast-message-text-content")).get(0);
        return errorDialog != null;
    }
}
