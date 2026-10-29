package com.qa.shop.utils;

/** The shop shows prices as "Rs. 1500"; tests compare whole rupees as integers. */
public final class Money {

    private Money() { }

    public static int parse(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("No amount in '" + text + "'");
        }
        return Integer.parseInt(digits);
    }
}
