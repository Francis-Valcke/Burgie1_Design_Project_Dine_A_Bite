package cobol.commons.order;

import java.util.Calendar;
import java.util.List;

public class CommonOrder {

    // unique id for this order
    private int id;

    private Calendar startTime;
    private Calendar expectedTime;

    private status orderStatus;

    private int standId;
    private String brandName;
    private String standName;

    //----- Request ------//
    private List<CommonOrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    private double latitude;
    private double longitude;

    public enum status {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }

    public int getId() {
        return id;
    }

    public int computeRemainingTime(){
        return (int) (expectedTime.getTimeInMillis()-startTime.getTimeInMillis());
    }

    public status getOrderStatus() {
        return orderStatus;
    }

    public int getStandId() {
        return standId;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getStandName() {
        return standName;
    }

    public List<CommonOrderItem> getOrderItems() {
        return orderItems;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
