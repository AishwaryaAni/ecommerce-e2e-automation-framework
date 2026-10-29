package com.qa.shop.base;

import com.qa.shop.api.ShopApi;
import com.qa.shop.data.UserFactory;
import com.qa.shop.driver.DriverFactory;
import com.qa.shop.models.User;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fresh browser per test, plus API-based test data:
 * customers are created through the API before the test (milliseconds, not a 17-field form)
 * and deleted through the API afterwards — the UI test focuses only on what it verifies.
 */
public abstract class BaseUiTest {

    protected final ShopApi api = new ShopApi();
    private final ThreadLocal<List<User>> createdUsers = ThreadLocal.withInitial(ArrayList::new);

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverFactory.start();
    }

    /** Creates a customer via the API and registers it for automatic cleanup. */
    protected User seedCustomer() {
        User user = api.seed(UserFactory.newCustomer());
        createdUsers.get().add(user);
        return user;
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (!result.isSuccess() && DriverFactory.isStarted()) {
                byte[] png = ((TakesScreenshot) DriverFactory.get()).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot at failure", "image/png", new ByteArrayInputStream(png), ".png");
                Allure.addAttachment("Page URL", DriverFactory.get().getCurrentUrl());
            }
        } finally {
            DriverFactory.quit();
            createdUsers.get().forEach(u -> api.deleteAccount(u.email(), u.password()));
            createdUsers.get().clear();
        }
    }
}
