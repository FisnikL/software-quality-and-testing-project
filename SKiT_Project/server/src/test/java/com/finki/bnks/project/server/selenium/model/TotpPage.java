package com.finki.bnks.project.server.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class TotpPage extends BasePage {

    public TotpPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() throws InterruptedException {
        Thread.sleep(3000);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("totp"))).isDisplayed();
    }

    public void verify() {
        driver.findElement(By.id("verify-button")).click();
    }

    public void insertCode(String code) throws InterruptedException {
        driver.findElement(By.xpath("//input[@id='code']")).clear();
        driver.findElement(By.xpath("//input[@id='code']")).sendKeys(code);
        Thread.sleep(5000);
    }

    public String getRequiredCodeErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("error-message"));
        return errorPage.getText();
    }

    public String getCodeLengthOfExactly6DigitsErrorMessage() {
        WebElement errorPage = driver.findElement(By.id("error-message"));
        return errorPage.getText();
    }
}
