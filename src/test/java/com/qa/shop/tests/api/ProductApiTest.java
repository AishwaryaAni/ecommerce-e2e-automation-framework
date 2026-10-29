package com.qa.shop.tests.api;

import com.qa.shop.api.ApiResult;
import com.qa.shop.api.ShopApi;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Map;

@Epic("Shop API")
@Feature("Catalogue")
public class ProductApiTest {

    private final ShopApi api = new ShopApi();

    @Test(groups = {"api", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Product list returns every product with complete, well-formed fields")
    public void productListIsComplete() {
        ApiResult result = api.productsList();
        Assert.assertEquals(result.code(), 200, "responseCode");

        List<Map<String, Object>> products = result.json().getList("products");
        Assert.assertFalse(products.isEmpty(), "Catalogue should not be empty");

        SoftAssert soft = new SoftAssert();
        for (Map<String, Object> p : products) {
            soft.assertNotNull(p.get("id"), "id present: " + p);
            soft.assertFalse(String.valueOf(p.get("name")).isBlank(), "name present: " + p);
            soft.assertTrue(String.valueOf(p.get("price")).matches("Rs\\. \\d+"), "price format 'Rs. N': " + p);
            soft.assertFalse(String.valueOf(p.get("brand")).isBlank(), "brand present: " + p);
            soft.assertNotNull(p.get("category"), "category present: " + p);
        }
        soft.assertAll();
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Product ids are unique across the catalogue")
    public void productIdsAreUnique() {
        List<Integer> ids = api.productsList().json().getList("products.id", Integer.class);
        Assert.assertEquals(ids.stream().distinct().count(), (long) ids.size(), "Duplicate product ids found: " + ids);
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Brand list returns named brands")
    public void brandListIsReturned() {
        ApiResult result = api.brandsList();
        Assert.assertEquals(result.code(), 200, "responseCode");
        List<String> brands = result.json().getList("brands.brand", String.class);
        Assert.assertFalse(brands.isEmpty(), "Brands should not be empty");
        Assert.assertTrue(brands.stream().noneMatch(String::isBlank), "No blank brand names: " + brands);
    }

    @Test(groups = {"api", "regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Every brand used by a product exists in the brand list (referential integrity)")
    public void productBrandsExistInBrandList() {
        List<String> brandList = api.brandsList().json().getList("brands.brand", String.class);
        List<String> productBrands = api.productsList().json().getList("products.brand", String.class);

        List<String> orphans = productBrands.stream().distinct().filter(b -> !brandList.contains(b)).toList();
        Assert.assertTrue(orphans.isEmpty(), "Product brands missing from brand list: " + orphans);
    }

    @Test(groups = {"api", "smoke"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Search returns matching products")
    public void searchReturnsProducts() {
        ApiResult result = api.searchProducts("top");
        Assert.assertEquals(result.code(), 200, "responseCode");
        Assert.assertFalse(result.json().getList("products").isEmpty(), "Search for 'top' returns products");
    }
}
