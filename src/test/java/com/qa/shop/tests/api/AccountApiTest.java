package com.qa.shop.tests.api;

import com.qa.shop.api.ApiResult;
import com.qa.shop.api.ShopApi;
import com.qa.shop.data.UserFactory;
import com.qa.shop.models.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Shop API")
@Feature("Customer accounts")
public class AccountApiTest {

    private final ShopApi api = new ShopApi();

    @Test(groups = {"api", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Full account lifecycle: create → verify → read → update → delete → verify gone")
    public void accountLifecycle() {
        User user = UserFactory.newCustomer();

        assertResult(api.createAccount(user), 201, "User created!");
        assertResult(api.verifyLogin(user.email(), user.password()), 200, "User exists!");

        ApiResult details = api.userDetails(user.email());
        Assert.assertEquals(details.code(), 200, "getUserDetailByEmail responseCode");
        Assert.assertEquals(details.json().getString("user.email"), user.email(), "Stored email");
        Assert.assertEquals(details.json().getString("user.first_name"), user.firstName(), "Stored first name");
        Assert.assertEquals(details.json().getString("user.city"), user.city(), "Stored city");

        User moved = user.withCity("Pune");
        assertResult(api.updateAccount(moved), 200, "User updated!");
        Assert.assertEquals(api.userDetails(user.email()).json().getString("user.city"), "Pune",
                "Update persisted");

        assertResult(api.deleteAccount(user.email(), user.password()), 200, "Account deleted!");
        assertResult(api.verifyLogin(user.email(), user.password()), 404, "User not found!");
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registering the same email twice is rejected")
    public void duplicateEmailIsRejected() {
        User user = UserFactory.newCustomer();
        try {
            assertResult(api.createAccount(user), 201, "User created!");
            ApiResult duplicate = api.createAccount(user);
            Assert.assertEquals(duplicate.code(), 400, "Duplicate registration must be rejected: " + duplicate.message());
        } finally {
            api.deleteAccount(user.email(), user.password());
        }
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Wrong password does not verify and the account is not deleted by a wrong password")
    public void wrongPasswordIsRejected() {
        User user = UserFactory.newCustomer();
        try {
            assertResult(api.createAccount(user), 201, "User created!");
            Assert.assertEquals(api.verifyLogin(user.email(), "not-the-password").code(), 404, "Wrong password");
            Assert.assertNotEquals(api.deleteAccount(user.email(), "not-the-password").code(), 200,
                    "Delete must require the correct password");
            Assert.assertEquals(api.verifyLogin(user.email(), user.password()).code(), 200, "Account still exists");
        } finally {
            api.deleteAccount(user.email(), user.password());
        }
    }

    @Step("Expect responseCode {expectedCode} and message \"{expectedMessage}\"")
    private void assertResult(ApiResult result, int expectedCode, String expectedMessage) {
        Assert.assertEquals(result.code(), expectedCode, "responseCode (message: " + result.message() + ")");
        Assert.assertEquals(result.message(), expectedMessage, "message");
    }
}
