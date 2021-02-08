package com.finki.bnks.project.server.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SignInSuccessfulPage extends BasePage {
    public SignInSuccessfulPage(WebDriver driver){
        super(driver);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sign-in-successful"))).isDisplayed();
    }
}
