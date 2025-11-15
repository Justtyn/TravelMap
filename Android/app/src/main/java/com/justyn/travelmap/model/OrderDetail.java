package com.justyn.travelmap.model;

import java.util.List;

public class OrderDetail {
    private final long orderId;
    private final String orderNo;
    private final String status;
    private final String orderType;
    private final double totalPrice;
    private final String contactName;
    private final String contactPhone;
    private final String createTime;
    private final String checkinDate;
    private final String checkoutDate;
    private final List<OrderItemDetail> items;

    public OrderDetail(long orderId,
                       String orderNo,
                       String status,
                       String orderType,
                       double totalPrice,
                       String contactName,
                       String contactPhone,
                       String createTime,
                       String checkinDate,
                       String checkoutDate,
                       List<OrderItemDetail> items) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.status = status;
        this.orderType = orderType;
        this.totalPrice = totalPrice;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.createTime = createTime;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.items = items;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderType() {
        return orderType;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getCheckinDate() {
        return checkinDate;
    }

    public String getCheckoutDate() {
        return checkoutDate;
    }

    public List<OrderItemDetail> getItems() {
        return items;
    }
}
