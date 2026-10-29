package com.qa.shop.api;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * The shop runs on a shared free host that occasionally answers 503/429.
 * Retrying those (never 4xx) separates infrastructure noise from real defects.
 * The back-off here is network retry logic, not UI synchronisation.
 */
public class RetryOnServerErrorFilter implements Filter {

    private final int maxAttempts;
    private final long backoffMs;

    public RetryOnServerErrorFilter(int maxAttempts, long backoffMs) {
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
    }

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification response,
                           FilterContext context) {
        Response result = context.next(request, response);
        for (int attempt = 2; attempt <= maxAttempts && isRetryable(result.statusCode()); attempt++) {
            pause(backoffMs * (attempt - 1));
            result = context.next(request, response);
        }
        return result;
    }

    private static boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
