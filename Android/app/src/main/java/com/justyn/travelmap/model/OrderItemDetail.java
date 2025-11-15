package com.justyn.travelmap.model;

public class OrderItemDetail {
    private final long orderItemId;
    private final int quantity;
    private final double price;
    private final FeedItem product;
    private final long scenicId;

    public OrderItemDetail(long orderItemId, int quantity, double price, FeedItem product, long scenicId) {
        this.orderItemId = orderItemId;
        this.quantity = quantity;
        this.price = price;
        this.product = product;
        this.scenicId = scenicId;
    }

    public long getOrderItemId() {
        return orderItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public FeedItem getProduct() {
        return product;
    }

    public long getScenicId() {
        return scenicId;
    }
}
