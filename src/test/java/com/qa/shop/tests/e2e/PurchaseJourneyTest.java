package com.qa.shop.tests.e2e;

import com.qa.shop.base.BaseUiTest;
import com.qa.shop.config.ConfigReader;
import com.qa.shop.models.CartLine;
import com.qa.shop.models.User;
import com.qa.shop.pages.CartPage;
import com.qa.shop.pages.CheckoutPage;
import com.qa.shop.pages.LoginPage;
import com.qa.shop.pages.OrderConfirmationPage;
import com.qa.shop.pages.ProductDetailsPage;
import com.qa.shop.pages.ProductsPage;
import com.qa.shop.utils.Money;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Epic("E-commerce")
@Feature("End-to-end purchase")
public class PurchaseJourneyTest extends BaseUiTest {

    private static final int BLUE_TOP = 1;
    private static final int MEN_TSHIRT = 2;

    @Test(groups = {"e2e", "ui", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("API-seeded customer logs in, buys two products (one with quantity 3), "
            + "pays, and downloads an invoice whose amount matches the cart")
    public void customerCompletesPurchaseAndDownloadsInvoice() {
        // ---- Arrange: data via API ----
        User customer = seedCustomer();
        Map<String, Integer> catalogPrices = catalogPricesFromApi();

        // ---- Login ----
        new LoginPage().open().loginAs(customer.email(), customer.password());
        Assert.assertEquals(new ProductsPage().loggedInUserName(), customer.name(), "Header shows the customer");

        // ---- Shop: one item from the grid, three of another from its details page ----
        new ProductsPage().open().addToCart(BLUE_TOP);
        CartPage cart = new ProductDetailsPage().open(MEN_TSHIRT).addQuantityAndViewCart(3);

        // ---- Cart: quantities, line maths, prices consistent with the API catalogue ----
        List<CartLine> lines = cart.lines();
        Assert.assertEquals(lines.size(), 2, "Two products in the cart");
        verifyCart(lines, catalogPrices, Map.of("Blue Top", 1, "Men Tshirt", 3));
        int expectedTotal = lines.stream().mapToInt(CartLine::lineTotal).sum();

        // ---- Checkout: address = the data we seeded; review = the cart ----
        CheckoutPage checkout = cart.proceedToCheckout();
        verifyDeliveryAddress(checkout.deliveryAddressText(), customer);
        Assert.assertEquals(checkout.reviewLines(), lines, "Order review matches the cart");

        // ---- Pay with the practice-site test card ----
        OrderConfirmationPage confirmation = checkout
                .placeOrderWithComment("Automated E2E order — please ignore")
                .payWithTestCard(customer.name(), ConfigReader.get("test.card.number"),
                        ConfigReader.get("test.card.cvc"), ConfigReader.get("test.card.month"),
                        ConfigReader.get("test.card.year"));
        Assert.assertTrue(confirmation.heading().equalsIgnoreCase("Order Placed!"),
                "Confirmation heading, got: " + confirmation.heading());

        // ---- Invoice: the downloaded document charges exactly the cart total ----
        String invoice = confirmation.downloadInvoiceText();
        Assert.assertTrue(invoice.contains(String.valueOf(expectedTotal)),
                "Invoice amount should be " + expectedTotal + " but invoice reads: " + invoice);
        Assert.assertTrue(invoice.contains(customer.firstName()), "Invoice addressed to the customer");
    }

    @Step("Fetch catalogue prices from the API")
    private Map<String, Integer> catalogPricesFromApi() {
        List<Map<String, Object>> products = api.productsList().json().getList("products");
        return products.stream().collect(Collectors.toMap(
                p -> String.valueOf(p.get("name")),
                p -> Money.parse(String.valueOf(p.get("price"))),
                (first, duplicate) -> first));
    }

    @Step("Verify cart lines")
    private void verifyCart(List<CartLine> lines, Map<String, Integer> catalogPrices, Map<String, Integer> expectedQty) {
        SoftAssert soft = new SoftAssert();
        for (CartLine line : lines) {
            soft.assertEquals(Integer.valueOf(line.quantity()), expectedQty.get(line.name()), "Quantity of " + line.name());
            soft.assertTrue(line.isLineTotalCorrect(), "Line total = price x qty for " + line.name() + ": " + line);
            soft.assertEquals(Integer.valueOf(line.unitPrice()), catalogPrices.get(line.name()),
                    "UI price of " + line.name() + " matches API catalogue");
        }
        soft.assertAll();
    }

    @Step("Verify delivery address matches the customer record")
    private void verifyDeliveryAddress(String address, User customer) {
        SoftAssert soft = new SoftAssert();
        for (String expected : List.of(customer.firstName(), customer.lastName(), customer.address1(),
                customer.city(), customer.state(), customer.zipcode(), customer.country(), customer.mobileNumber())) {
            soft.assertTrue(address.contains(expected), "Delivery address contains '" + expected + "' — actual: " + address);
        }
        soft.assertAll();
    }
}
