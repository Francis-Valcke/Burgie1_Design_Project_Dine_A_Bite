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
        System.out.println("NU MAKEN WE HET AAN:" + expectedTime);
        System.out.println(this.expectedTime);
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
        System.out.println("ZITTEN HIER NU EH");
        System.out.println(expectedTime);
        System.out.println(Calendar.getInstance());
        System.out.println("EN DIT IS:");
        System.out.println((int) ((expectedTime.getTimeInMillis()-Calendar.getInstance().getTimeInMillis())/1000));
        return (int) ((expectedTime.getTimeInMillis()-Calendar.getInstance().getTimeInMillis())/1000);
    }

}
