package cobol.commons.order;

import lombok.Data;

import java.util.Calendar;
import java.util.List;

@Data
public class CommonOrder {

    // unique id for this order
    private int id;

    private Calendar startTime;
    private Calendar expectedTime;
    private State orderState;
    private String brandName;
    private String standName;

    //----- Request ------//
    private List<CommonOrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    private double latitude;
    private double longitude;

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

    public int computeRemainingTime(){
        return (int) (expectedTime.getTimeInMillis()-startTime.getTimeInMillis());
    }

}
