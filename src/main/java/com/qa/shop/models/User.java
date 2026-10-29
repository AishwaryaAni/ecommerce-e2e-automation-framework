package com.qa.shop.models;

import java.util.LinkedHashMap;
import java.util.Map;

/** A shop customer. Field names mirror the createAccount API form parameters. */
public record User(
        String name, String email, String password, String title,
        String birthDate, String birthMonth, String birthYear,
        String firstName, String lastName, String company,
        String address1, String address2, String country,
        String zipcode, String state, String city, String mobileNumber) {

    /** Form body for POST /createAccount and PUT /updateAccount. */
    public Map<String, String> toFormParams() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("name", name);
        p.put("email", email);
        p.put("password", password);
        p.put("title", title);
        p.put("birth_date", birthDate);
        p.put("birth_month", birthMonth);
        p.put("birth_year", birthYear);
        p.put("firstname", firstName);
        p.put("lastname", lastName);
        p.put("company", company);
        p.put("address1", address1);
        p.put("address2", address2);
        p.put("country", country);
        p.put("zipcode", zipcode);
        p.put("state", state);
        p.put("city", city);
        p.put("mobile_number", mobileNumber);
        return p;
    }

    public User withCity(String newCity) {
        return new User(name, email, password, title, birthDate, birthMonth, birthYear, firstName, lastName,
                company, address1, address2, country, zipcode, state, newCity, mobileNumber);
    }
}
