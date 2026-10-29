package com.qa.shop.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** /products — catalogue grid, search, and the "Added!" cart modal. */
public class ProductsPage extends BasePage {

    private final By searchInput = By.id("search_product");
    private final By searchButton = By.id("submit_search");
    private final By sectionTitle = By.cssSelector(".features_items .title");
    private final By productNames = By.cssSelector(".features_items .productinfo p");
    private final By cartModal = By.id("cartModal");
    private final By continueShopping = By.cssSelector("#cartModal button.close-modal");

    @Step("Open the products page")
    public ProductsPage open() {
        openPath("/products");
        visible(searchInput);
        return this;
    }

    @Step("Search the catalogue for \"{term}\"")
    public ProductsPage search(String term) {
        type(searchInput, term);
        click(searchButton);
        // Heading is CSS-uppercased, so compare case-insensitively
        wait.until(d -> d.findElement(sectionTitle).getText().trim().equalsIgnoreCase("Searched Products"));
        return this;
    }

    /** Names of the products currently shown in the grid. */
    public List<String> visibleProductNames() {
        return driver.findElements(productNames).stream().map(WebElement::getText).map(String::trim).toList();
    }

    @Step("Add product #{productId} to the cart from the grid")
    public ProductsPage addToCart(int productId) {
        jsClick(By.cssSelector(".productinfo a.add-to-cart[data-product-id='" + productId + "']"));
        visible(cartModal);
        click(continueShopping);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(cartModal));
        return this;
    }
}
