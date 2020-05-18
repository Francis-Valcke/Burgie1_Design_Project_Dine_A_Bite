package com.example.standapp.order;

/**
 * Class for sending an order status update of an order to the Event Channel
 */
public class CommonOrderStatusUpdate {

    private int orderId;
    private State mNewState;

    public enum State {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED, // START
        READY, // DONE
        PICKED_UP
    }

    public CommonOrderStatusUpdate() {}

    public CommonOrderStatusUpdate(int orderId, State newState) {
        this.orderId = orderId;
        this.mNewState = newState;
    }

    public int getOrderId() {
        return orderId;
    }

    public State getNewState() {
        return mNewState;
    }

    public static State convertStatus(CommonOrder.State State) {
        switch (State) {
            case SEND:
                return CommonOrderStatusUpdate.State.SEND;
            case PENDING:
                return CommonOrderStatusUpdate.State.PENDING;
            case DECLINED:
                return CommonOrderStatusUpdate.State.DECLINED;
            case CONFIRMED:
                return CommonOrderStatusUpdate.State.CONFIRMED;
            case READY:
                return CommonOrderStatusUpdate.State.READY;
            default:
                // includes CommonOrder.State.PICKED_UP
                return CommonOrderStatusUpdate.State.PICKED_UP;
        }
    }
}
