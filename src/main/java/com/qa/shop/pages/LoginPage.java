package com.qa.shop.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** /login — existing-customer login form (left side of the page). */
public class LoginPage extends BasePage {

    private final By email = By.cssSelector("[data-qa='login-email']");
    private final By password = By.cssSelector("[data-qa='login-password']");
    private final By loginButton = By.cssSelector("[data-qa='login-button']");
    private final By error = By.xpath("//form[@action='/login']//p");

    @Step("Open the login page")
    public LoginPage open() {
        openPath("/login");
        visible(loginButton);
        return this;
    }

    @Step("Log in as {userEmail}")
    public HomePage loginAs(String userEmail, String userPassword) {
        submit(userEmail, userPassword);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        return new HomePage();
    }

    @Step("Attempt login with invalid credentials")
    public LoginPage loginExpectingError(String userEmail, String userPassword) {
        submit(userEmail, userPassword);
        return this;
    }

    public String errorMessage() {
        return text(error);
    }

    private void submit(String userEmail, String userPassword) {
        type(email, userEmail);
        type(password, userPassword);
        click(loginButton);
    }
}
