package com.qa.shop.tests.ui;

import com.qa.shop.base.BaseUiTest;
import com.qa.shop.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cross-layer data check: what the customer sees in the UI must equal what the API
 * (the system of record) returns for the same search — the UI equivalent of
 * reconciling a report against the database.
 */
@Epic("E-commerce")
@Feature("Search: UI vs API consistency")
public class SearchConsistencyTest extends BaseUiTest {

    @DataProvider(name = "searchTerms")
    public Object[][] searchTerms() {
        return new Object[][]{{"top"}, {"jeans"}, {"dress"}, {"tshirt"}};
    }

    @Test(dataProvider = "searchTerms", groups = {"ui", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("UI search results show exactly the products the API returns")
    public void uiSearchMatchesApi(String term) {
        List<String> fromApi = sorted(api.searchProductNames(term));
        List<String> fromUi = sorted(new ProductsPage().open().search(term).visibleProductNames());

        Assert.assertFalse(fromApi.isEmpty(), "API should return results for '" + term + "'");
        Assert.assertEquals(fromUi, fromApi, "UI results for '" + term + "' match the API");
    }

    private static List<String> sorted(List<String> names) {
        List<String> copy = new ArrayList<>(names);
        Collections.sort(copy);
        return copy;
    }
}
