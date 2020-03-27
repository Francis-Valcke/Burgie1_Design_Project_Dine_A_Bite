package cobol.commons.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.util.*;

public class Order {

    // Static counter that keeps track of number of order
    // TODO: needs to change, counter must be kept in database
    private static int orderCounter = 1;


    //----- Backend Information -----//

    // unique id for this order
    private int id;

    // Remaining time in seconds
    private Calendar startTime;
    private Calendar expectedTime;
    private status orderStatus;
    private int standId;
    private String brandName;
    private String standName;

    //----- Request ------//

    private ArrayList<OrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    private double latitude;
    private double longitude;


    /**
     * Constructs an order object from a JSON file
     *
     * @param orderFile JSON file received from the attendee-app
     *
     * TODO:
     *     - id needs to be updated with respect to database
     *     - remainingTimeSec needs to be dynamic
     *     - momenteel keys (en bij new food) gewoon vaste prijs en preptime
     */
    public Order(JSONObject orderFile) throws JsonProcessingException {

        // Read in request
        ObjectMapper mapper = new ObjectMapper();
        Order temp=mapper.readValue(orderFile.toJSONString(), Order.class);

        this.latitude = temp.getLatitude();
        this.longitude= temp.getLongitude();
        this.orderItems= new ArrayList<>(temp.getOrderItems());

        // Add new information
        // TODO needs to be fetched from database or something else
        this.id=orderCounter;

        this.orderStatus=status.PENDING;

        // set time
        // TODO needs to be asked from StandManager
        this.startTime=Calendar.getInstance();
        this.expectedTime=Calendar.getInstance();
        expectedTime.setTime(startTime.getTime());
        expectedTime.add(Calendar.MINUTE, 15);

        // Update ID counter
        orderCounter++;
    }


    public Order() {
        this.id=-1;
    }


    // ---- Getters and Setters ----- //

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public ArrayList<OrderItem> getOrderItems() {
        return orderItems;
    }

    // TODO: these 2 functions only temporary for remaining time
    //  (think this should be through event channel)
    public int getRemainingTime() {
        return (int) (expectedTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
    }

    // Will only have to be called once (when the standmanager accepts the order)
    public void setRemtime(int remtime) {
        expectedTime.setTime(startTime.getTime());
        expectedTime.add(Calendar.MILLISECOND, remtime);
    }

    public int getId() {
        return id;
    }

    public status getOrderStatus() {
        return orderStatus;
    }

    public void setState(status state) {
        this.orderStatus = state;
    }

    public int getStandId() {
        return standId;
    }

    public void setStandId(int standId) {
        this.standId = standId;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", remainingTimeSec=" + getRemainingTime() +
                ", orderStatus=" + orderStatus +
                ", orderItems=" + orderItems +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public enum status {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }
}
