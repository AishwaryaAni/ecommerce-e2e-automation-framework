package com.qa.shop.models;

/** One row of the cart / order review table, as the customer sees it. */
public record CartLine(String name, int unitPrice, int quantity, int lineTotal) {

    public boolean isLineTotalCorrect() {
        return unitPrice * quantity == lineTotal;
    }
}
