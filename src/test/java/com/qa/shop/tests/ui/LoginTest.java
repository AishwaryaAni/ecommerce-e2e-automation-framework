package com.qa.shop.tests.ui;

import com.qa.shop.base.BaseUiTest;
import com.qa.shop.models.User;
import com.qa.shop.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("E-commerce")
@Feature("Login")
public class LoginTest extends BaseUiTest {

    @Test(groups = {"ui", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("A customer created through the API can log in through the UI")
    public void apiSeededCustomerCanLogIn() {
        User customer = seedCustomer();

        String shownName = new LoginPage().open()
                .loginAs(customer.email(), customer.password())
                .loggedInUserName();

        Assert.assertEquals(shownName, customer.name(), "Header greets the customer by name");
    }

    @Test(groups = {"ui", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Wrong password is rejected with a clear message")
    public void wrongPasswordIsRejected() {
        User customer = seedCustomer();

        String error = new LoginPage().open()
                .loginExpectingError(customer.email(), customer.password() + "-wrong")
                .errorMessage();

        Assert.assertEquals(error, "Your email or password is incorrect!", "Login error message");
    }
}
