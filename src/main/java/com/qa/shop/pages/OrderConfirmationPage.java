package com.qa.shop.pages;

import com.qa.shop.driver.DriverFactory;
import com.qa.shop.utils.Downloads;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.nio.file.Path;
import java.time.Duration;

/** Order placed — confirmation message and invoice download. */
public class OrderConfirmationPage extends BasePage {

    private final By heading = By.cssSelector("[data-qa='order-placed']");
    private final By downloadInvoice = By.cssSelector("a[href^='/download_invoice/']");

    public OrderConfirmationPage() {
        visible(heading);
    }

    public String heading() {
        return text(heading);
    }

    @Step("Download the invoice")
    public String downloadInvoiceText() {
        click(downloadInvoice);
        Path invoice = Downloads.waitForFile(DriverFactory.downloadDir(), ".txt", Duration.ofSeconds(30));
        return Downloads.read(invoice);
    }
}
