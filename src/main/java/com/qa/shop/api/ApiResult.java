package com.qa.shop.api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * This API always answers HTTP 200 and puts the real status in a "responseCode" field
 * (and serves JSON as text/html). ApiResult parses the body itself so tests assert on
 * the code that actually carries meaning.
 */
public final class ApiResult {

    private final Response raw;
    private final JsonPath json;

    ApiResult(Response raw) {
        this.raw = raw;
        this.json = JsonPath.from(raw.asString());
    }

    public int httpStatus() {
        return raw.statusCode();
    }

    /** The application-level status, e.g. 201 for "User created!". */
    public int code() {
        return json.getInt("responseCode");
    }

    public String message() {
        return json.getString("message");
    }

    public JsonPath json() {
        return json;
    }

    public long timeMs() {
        return raw.time();
    }
}
