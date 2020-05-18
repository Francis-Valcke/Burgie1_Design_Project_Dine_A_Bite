package com.example.attendeeapp.json;

import java.io.Serializable;

/**
 * Class for sending an order status update of an order to the Event Channel
 */
public class CommonOrderStatusUpdate implements Serializable {

    private int orderId;
    private State newState;

    public enum State {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED, // START
        READY, // DONE
        PICKED_UP,
        BEGUN
    }

    public CommonOrderStatusUpdate() {}

    public CommonOrderStatusUpdate(int orderId, State newState) {
        this.orderId = orderId;
        this.newState = newState;
    }

    public int getOrderId() {
        return orderId;
    }

    public State getNewState() {
        return newState;
    }
}
