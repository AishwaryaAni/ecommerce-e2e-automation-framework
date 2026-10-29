package com.qa.shop.tests.api;

import com.qa.shop.api.ApiResult;
import com.qa.shop.api.ShopApi;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Error handling and HTTP contract. The API signals errors only in the body;
 * the transport status is always 200 — documented in the README as a finding.
 */
@Epic("Shop API")
@Feature("Error handling & HTTP contract")
public class ApiContractTest {

    private final ShopApi api = new ShopApi();

    @DataProvider(name = "unsupportedMethods")
    public Object[][] unsupportedMethods() {
        return new Object[][]{
                {"POST", "/productsList"},
                {"PUT", "/brandsList"},
                {"DELETE", "/verifyLogin"},
        };
    }

    @Test(dataProvider = "unsupportedMethods", groups = {"api", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Unsupported HTTP methods are refused with responseCode 405")
    public void unsupportedMethodIsRefused(String method, String path) {
        ApiResult result = api.call(method, path);
        Assert.assertEquals(result.code(), 405, "responseCode");
        Assert.assertEquals(result.message(), "This request method is not supported.", "message");
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Search without its required parameter is a bad request")
    public void searchWithoutParameterIsBadRequest() {
        ApiResult result = api.searchWithoutTerm();
        Assert.assertEquals(result.code(), 400, "responseCode");
        Assert.assertEquals(result.message(), "Bad request, search_product parameter is missing in POST request.");
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Login verification without email is a bad request")
    public void verifyLoginWithoutEmailIsBadRequest() {
        ApiResult result = api.verifyLoginWithoutEmail("any-password");
        Assert.assertEquals(result.code(), 400, "responseCode");
        Assert.assertEquals(result.message(), "Bad request, email or password parameter is missing in POST request.");
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.MINOR)
    @Description("Known defect, pinned: error responses still use HTTP 200 instead of 4xx")
    public void errorsAreSignalledInBodyNotHttpStatus() {
        ApiResult result = api.call("POST", "/productsList");
        Assert.assertEquals(result.code(), 405, "Body reports the real error");
        Assert.assertEquals(result.httpStatus(), 200,
                "Transport status is 200 today. If this assertion fails, the API has been fixed — update the test.");
    }
}
