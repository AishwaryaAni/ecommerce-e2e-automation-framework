package com.qa.shop.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** /payment — practice-site card form (accepts test card data only; no real payment). */
public class PaymentPage extends BasePage {

    private final By nameOnCard = By.cssSelector("[data-qa='name-on-card']");
    private final By cardNumber = By.cssSelector("[data-qa='card-number']");
    private final By cvc = By.cssSelector("[data-qa='cvc']");
    private final By expiryMonth = By.cssSelector("[data-qa='expiry-month']");
    private final By expiryYear = By.cssSelector("[data-qa='expiry-year']");
    private final By payButton = By.cssSelector("[data-qa='pay-button']");

    public PaymentPage() {
        wait.until(ExpectedConditions.urlContains("/payment"));
    }

    @Step("Pay with the practice test card")
    public OrderConfirmationPage payWithTestCard(String holder, String number, String cvcCode, String month, String year) {
        type(nameOnCard, holder);
        type(cardNumber, number);
        type(cvc, cvcCode);
        type(expiryMonth, month);
        type(expiryYear, year);
        click(payButton);
        return new OrderConfirmationPage();
    }
}
