package cobol.services.ordermanager.dbmenu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name="orders")
public class Order implements Serializable {

    // Static counter that keeps track of number of order
    // TODO: needs to change, counter must be kept in database
    private static int orderCounter = 1;


    //----- Backend Information -----//

    // unique id for this order
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "datetime")
    private Calendar startTime;
    @Column(columnDefinition = "datetime")
    private Calendar expectedTime;
    @Column
    private status orderStatus;
    @Column
    private int standId;
    @Column
    private String brandName;
    @Column
    private String standName;

    //----- Request ------//
    @OneToMany(
            targetEntity = OrderItem.class,
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    @Column
    private double latitude;
    @Column
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
        this.orderItems=new ArrayList<>();
        for (OrderItem orderItem : temp.getOrderItems()) {
            this.addOrderItem(orderItem);
        }
        // Add new information
        this.startTime=Calendar.getInstance();
        this.expectedTime=Calendar.getInstance();
        expectedTime.setTime(startTime.getTime());
        expectedTime.add(Calendar.MINUTE, 15);

    }


    public Order() {
    }


    // ---- Getters and Setters ----- //

    public void addOrderItem(OrderItem item){
        orderItems.add(item);
        item.setOrder(this);
    }



    public void removeOrderItem(OrderItem item){
        orderItems.remove(item);
        item.setOrder(this);
    }

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

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    // TODO: these 2 functions only temporary for remaining time
    //  (think this should be through event channel)
    public int computeRemainingTime() {
        return (int) (expectedTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
    }

    // Will only have to be called once (when the standmanager accepts the order)
    public void setRemtime(int remtime) {
        expectedTime.setTime(startTime.getTime());
        expectedTime.add(Calendar.MILLISECOND, remtime);
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    public void setId(int id){
        this.id=id;
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
                ", remainingTimeSec=" + this.computeRemainingTime() +
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
