package com.qa.shop.data;

import com.qa.shop.models.User;
import net.datafaker.Faker;

import java.util.Locale;
import java.util.UUID;

/** Fresh, unique customer per test — no shared accounts, no clashes between parallel runs. */
public final class UserFactory {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);
    private static final String[] COUNTRIES = {"India", "United States", "Canada", "Australia", "Singapore"};

    private UserFactory() { }

    public static User newCustomer() {
        String first = FAKER.name().firstName().replaceAll("[^A-Za-z]", "");
        String last = FAKER.name().lastName().replaceAll("[^A-Za-z]", "");
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return new User(
                first + " " + last,
                "qa." + first.toLowerCase() + "." + unique + "@example.com",
                "Pw!" + unique,
                FAKER.options().option("Mr", "Mrs", "Miss"),
                String.valueOf(FAKER.number().numberBetween(1, 28)),
                String.valueOf(FAKER.number().numberBetween(1, 12)),
                String.valueOf(FAKER.number().numberBetween(1970, 2003)),
                first,
                last,
                FAKER.company().name().replaceAll("[^A-Za-z0-9 ]", ""),
                FAKER.number().numberBetween(1, 999) + " " + FAKER.address().streetName().replaceAll("[^A-Za-z ]", ""),
                "Suite " + FAKER.number().numberBetween(1, 99),
                FAKER.options().option(COUNTRIES),
                String.valueOf(FAKER.number().numberBetween(100000, 999999)),
                FAKER.address().state().replaceAll("[^A-Za-z ]", ""),
                FAKER.address().city().replaceAll("[^A-Za-z ]", ""),
                "9" + FAKER.number().digits(9));
    }
}
