package com.justyn.travelmap.model;

public class CartItem {
    private final long cartId;
    private final int quantity;
    private final FeedItem product;

    public CartItem(long cartId, int quantity, FeedItem product) {
        this.cartId = cartId;
        this.quantity = quantity;
        this.product = product;
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
}
