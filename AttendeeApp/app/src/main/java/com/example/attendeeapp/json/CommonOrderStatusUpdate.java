package com.example.attendeeapp.json;

import java.io.Serializable;

/**
 * Class for sending an order status update of an order to the Event Channel
 */
public class CommonOrderStatusUpdate implements Serializable {

    private int orderId;
    private status newStatus;

    public enum status {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED, // START
        READY, // DONE
        PICKED_UP
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
