package com.qa.shop.pages;

import com.qa.shop.utils.Money;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** /product_details/{id} — price, quantity selector, add to cart. */
public class ProductDetailsPage extends BasePage {

    private final By name = By.cssSelector(".product-information h2");
    private final By price = By.cssSelector(".product-information span span");
    private final By quantity = By.id("quantity");
    private final By addToCart = By.cssSelector(".product-information button.cart");
    private final By viewCartInModal = By.cssSelector("#cartModal a[href='/view_cart']");

    @Step("Open product details #{productId}")
    public ProductDetailsPage open(int productId) {
        openPath("/product_details/" + productId);
        visible(name);
        return this;
    }

    public String productName() {
        return text(name);
    }

    public int unitPrice() {
        return Money.parse(text(price));
    }

    @Step("Add {qty} unit(s) to the cart and go to the cart")
    public CartPage addQuantityAndViewCart(int qty) {
        type(quantity, String.valueOf(qty));
        click(addToCart);
        click(viewCartInModal);
        return new CartPage();
    }
}
