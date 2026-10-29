package com.qa.shop.pages;

import com.qa.shop.models.CartLine;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** /checkout — delivery address, order review, comment, place order. */
public class CheckoutPage extends BasePage {

    private final By deliveryAddress = By.id("address_delivery");
    private final By comment = By.cssSelector("textarea[name='message']");
    private final By placeOrder = By.cssSelector("a[href='/payment']");

    public CheckoutPage() {
        wait.until(ExpectedConditions.urlContains("/checkout"));
        visible(deliveryAddress);
    }

    public String deliveryAddressText() {
        return text(deliveryAddress);
    }

    @Step("Read the order review lines")
    public List<CartLine> reviewLines() {
        wait.until(ExpectedConditions.presenceOfElementLocated(CartPage.ROWS));
        return CartPage.readLines(driver.findElements(CartPage.ROWS));
    }

    @Step("Add an order comment and place the order")
    public PaymentPage placeOrderWithComment(String text) {
        type(comment, text);
        click(placeOrder);
        return new PaymentPage();
    }
}
