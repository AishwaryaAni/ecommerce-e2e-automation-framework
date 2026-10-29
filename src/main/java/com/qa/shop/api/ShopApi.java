package com.qa.shop.api;

import com.qa.shop.config.ConfigReader;
import com.qa.shop.models.User;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Client for the shop's public API. Used two ways:
 *  - as the system under test (API test suite), and
 *  - as a fast test-data tool for UI tests (create / delete customers in milliseconds
 *    instead of clicking through the registration form).
 */
public class ShopApi {

    private static final RequestSpecification SPEC = new RequestSpecBuilder()
            .setBaseUri(ConfigReader.get("apiUrl"))
            .addFilter(new RetryOnServerErrorFilter(ConfigReader.getInt("api.retry.attempts"), 2000))
            .addFilter(new AllureRestAssured())
            .build();

    private RequestSpecification request() {
        return given().spec(SPEC);
    }

    // ---------- Products ----------
    @Step("API: GET /productsList")
    public ApiResult productsList() {
        return new ApiResult(request().get("/productsList"));
    }

    @Step("API: GET /brandsList")
    public ApiResult brandsList() {
        return new ApiResult(request().get("/brandsList"));
    }

    @Step("API: search products for \"{term}\"")
    public ApiResult searchProducts(String term) {
        return new ApiResult(request().formParam("search_product", term).post("/searchProduct"));
    }

    @Step("API: POST /searchProduct without search_product")
    public ApiResult searchWithoutTerm() {
        return new ApiResult(request().post("/searchProduct"));
    }

    /** Product names returned by search — the reference data for UI consistency checks. */
    public List<String> searchProductNames(String term) {
        ApiResult result = searchProducts(term);
        if (result.code() != 200) {
            throw new IllegalStateException("searchProduct failed: " + result.code() + " " + result.message());
        }
        return result.json().getList("products.name", String.class);
    }

    /** Generic call used by the "unsupported method" data-driven test. */
    @Step("API: {method} {path}")
    public ApiResult call(String method, String path) {
        return new ApiResult(request().request(method, path));
    }

    // ---------- Accounts ----------
    @Step("API: create account for {user.email}")
    public ApiResult createAccount(User user) {
        return new ApiResult(request().formParams(user.toFormParams()).post("/createAccount"));
    }

    @Step("API: update account for {user.email}")
    public ApiResult updateAccount(User user) {
        return new ApiResult(request().formParams(user.toFormParams()).put("/updateAccount"));
    }

    @Step("API: verify login for {email}")
    public ApiResult verifyLogin(String email, String password) {
        return new ApiResult(request().formParam("email", email).formParam("password", password).post("/verifyLogin"));
    }

    @Step("API: verify login without email")
    public ApiResult verifyLoginWithoutEmail(String password) {
        return new ApiResult(request().formParam("password", password).post("/verifyLogin"));
    }

    @Step("API: get user details for {email}")
    public ApiResult userDetails(String email) {
        return new ApiResult(request().queryParam("email", email).get("/getUserDetailByEmail"));
    }

    @Step("API: delete account {email}")
    public ApiResult deleteAccount(String email, String password) {
        return new ApiResult(request().formParam("email", email).formParam("password", password).delete("/deleteAccount"));
    }

    /** Test-data helper: create a customer and fail fast if the API refuses. */
    public User seed(User user) {
        ApiResult result = createAccount(user);
        if (result.code() != 201) {
            throw new IllegalStateException("Could not seed user: " + result.code() + " " + result.message());
        }
        return user;
    }
}
