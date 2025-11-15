package com.justyn.travelmap.model;

public class CartItem {
    private final long cartId;
    private final int quantity;
    private final FeedItem product;
    private final double unitPrice;

    public CartItem(long cartId, int quantity, FeedItem product, double unitPrice) {
        this.cartId = cartId;
        this.quantity = quantity;
        this.product = product;
        this.unitPrice = unitPrice;
    }

    public long getCartId() {
        return cartId;
    }

    public int getQuantity() {
        return quantity;
    }

    public FeedItem getProduct() {
        return product;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
