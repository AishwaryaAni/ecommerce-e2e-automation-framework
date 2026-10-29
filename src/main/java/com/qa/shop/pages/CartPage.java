package com.qa.shop.pages;

import com.qa.shop.config.ConfigReader;
import com.qa.shop.driver.DriverFactory;
import com.qa.shop.models.CartLine;
import com.qa.shop.utils.Money;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** /view_cart — line items, quantities, totals, checkout entry point. */
public class CartPage extends BasePage {

    static final By ROWS = By.cssSelector("tr[id^='product-']");
    private final By proceedToCheckout = By.cssSelector("a.check_out");
    private final By emptyCart = By.id("empty_cart");

    public CartPage() {
        wait.until(ExpectedConditions.urlContains("/view_cart"));
    }

    @Step("Open the cart")
    public static CartPage open() {
        DriverFactory.get().get(ConfigReader.get("baseUrl") + "/view_cart");
        return new CartPage();
    }

    @Step("Read the cart lines")
    public List<CartLine> lines() {
        wait.until(ExpectedConditions.presenceOfElementLocated(ROWS));
        return readLines(driver.findElements(ROWS));
    }

    @Step("Remove \"{productName}\" from the cart")
    public CartPage remove(String productName) {
        By row = By.xpath("//tr[starts-with(@id,'product-')][.//td[@class='cart_description']//a[normalize-space()='" + productName + "']]");
        WebElement line = visible(row);
        line.findElement(By.cssSelector("a.cart_quantity_delete")).click();
        wait.until(ExpectedConditions.stalenessOf(line));
        return this;
    }

    public boolean isEmpty() {
        return visible(emptyCart).isDisplayed();
    }

    @Step("Proceed to checkout")
    public CheckoutPage proceedToCheckout() {
        click(proceedToCheckout);
        return new CheckoutPage();
    }

    /** Shared with CheckoutPage — the review table has the same row structure. */
    static List<CartLine> readLines(List<WebElement> rows) {
        return rows.stream().map(r -> new CartLine(
                r.findElement(By.cssSelector(".cart_description h4 a")).getText().trim(),
                Money.parse(r.findElement(By.cssSelector(".cart_price p")).getText()),
                Integer.parseInt(r.findElement(By.cssSelector(".cart_quantity button")).getText().trim()),
                Money.parse(r.findElement(By.cssSelector(".cart_total p")).getText())
        )).toList();
    }
}
