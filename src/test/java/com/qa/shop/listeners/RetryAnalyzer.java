package com.qa.shop.listeners;

import com.qa.shop.config.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a failed UI test once (configurable). The site under test is a public,
 * ad-funded shop on shared hosting; one retry absorbs infrastructure noise, while a
 * genuine defect still fails twice and is reported. Every retry is visible in Allure.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private final int maxRetries = ConfigReader.getInt("ui.retry.count");
    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < maxRetries) {
            attempts++;
            return true;
        }
        return false;
    }
}
