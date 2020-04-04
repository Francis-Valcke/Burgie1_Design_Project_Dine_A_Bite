package cobol.commons.order;

import lombok.Data;

import java.util.Calendar;
import java.util.List;

@Data
public class CommonOrder {

    private int id;

    private Calendar startTime;
    private Calendar expectedTime;
    private State orderState;
    private String brandName;
    private String standName;

    private List<CommonOrderItem> orderItems;

    // Coordinates attendee on moment that order was made
    private double latitude;
    private double longitude;

    public CommonOrder(){}

    public CommonOrder(int id, Calendar startTime, Calendar expectedTime, State orderState, String brandName, String standName, List<CommonOrderItem> orderItems, double latitude, double longitude) {
        this.id = id;
        this.startTime = startTime;
        this.expectedTime = expectedTime;
        this.orderState = orderState;
        this.brandName = brandName;
        this.standName = standName;
        this.orderItems = orderItems;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public enum State {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }

    /**
     * Computes remaining time till expected time with respect to current time
     * @return RemainingTime in seconds
     */
    public int computeRemainingTime(){
        return (int) (expectedTime.getTimeInMillis()-startTime.getTimeInMillis())/1000;
    }

}
