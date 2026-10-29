package com.qa.shop.pages;

import com.qa.shop.config.ConfigReader;
import com.qa.shop.driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Explicit-wait helpers shared by every page; the header is common to all pages. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    private final By loggedInAs = By.xpath("//a[contains(normalize-space(.),'Logged in as')]/b");

    protected BasePage() {
        this.driver = DriverFactory.get();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait")));
    }

    protected void openPath(String path) {
        driver.get(ConfigReader.get("baseUrl") + path);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Scrolls the element to the centre and clicks it via JS. Used for product-grid
     * buttons that sit under hover overlays/sticky elements and otherwise throw
     * ElementClickInterceptedException intermittently.
     */
    protected void jsClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    protected void type(By locator, String text) {
        WebElement el = visible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    /** Header check available on every page: "Logged in as <name>". */
    public String loggedInUserName() {
        return text(loggedInAs);
    }
}
