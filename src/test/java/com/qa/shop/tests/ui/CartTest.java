package com.qa.shop.tests.ui;

import com.qa.shop.base.BaseUiTest;
import com.qa.shop.models.CartLine;
import com.qa.shop.pages.CartPage;
import com.qa.shop.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Epic("E-commerce")
@Feature("Cart")
public class CartTest extends BaseUiTest {

    @Test(groups = {"ui", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Guest adds two products, removes them one by one, and ends with an empty cart")
    public void addAndRemoveItems() {
        new ProductsPage().open().addToCart(1).addToCart(2);

        CartPage cart = CartPage.open();
        List<String> names = cart.lines().stream().map(CartLine::name).toList();
        Assert.assertEquals(names, List.of("Blue Top", "Men Tshirt"), "Cart contents in add order");

        cart.remove("Blue Top");
        Assert.assertEquals(cart.lines().stream().map(CartLine::name).toList(), List.of("Men Tshirt"),
                "Only the remaining product is left");

        cart.remove("Men Tshirt");
        Assert.assertTrue(cart.isEmpty(), "Empty-cart message is shown");
    }

    @Test(groups = {"ui", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Adding the same product twice increases its quantity instead of duplicating the line")
    public void addingSameProductTwiceIncrementsQuantity() {
        new ProductsPage().open().addToCart(1).addToCart(1);

        List<CartLine> lines = CartPage.open().lines();
        Assert.assertEquals(lines.size(), 1, "One cart line for the product");
        Assert.assertEquals(lines.get(0).quantity(), 2, "Quantity incremented");
        Assert.assertTrue(lines.get(0).isLineTotalCorrect(), "Line total = price x qty: " + lines.get(0));
    }
}
