package com.example.standapp.order;

public class CommonOrderStatusUpdate {

    private int orderId;
    private status newStatus;

    public enum status {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }

    public CommonOrderStatusUpdate() {}

    public CommonOrderStatusUpdate(int orderId, status newStatus) {
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public status getNewStatus() {
        return newStatus;
    }
}
