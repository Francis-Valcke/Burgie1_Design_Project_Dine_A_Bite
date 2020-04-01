package cobol.services.ordermanager.domain.entity;

import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.Modifying;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
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
    private CommonOrder.State orderState;

    @ManyToOne
    @JoinColumns(
            foreignKey = @ForeignKey(name = "order_stand_fk"), value = {
            @JoinColumn(referencedColumnName = "name", name = "stand_name"),
            @JoinColumn(referencedColumnName = "brand_name", name = "brand_name")
    }
    )
    private Stand stand;

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


    public Order() {
    }

    /**
     * Constructs an order object from a JSON file
     *
     * @param orderObject CommonOrder object
     */
    public Order(CommonOrder orderObject) {
        this.latitude = orderObject.getLatitude();
        this.longitude = orderObject.getLongitude();
        this.orderItems = new ArrayList<>();
        for (CommonOrderItem orderItem : orderObject.getOrderItems()) {
            this.addOrderItem(new OrderItem(orderItem, this));
        }
        this.orderState=orderObject.getOrderState();
        this.startTime = Calendar.getInstance();
        this.expectedTime = Calendar.getInstance();
        expectedTime.setTime(startTime.getTime());
        expectedTime.add(Calendar.MINUTE, 15);

    }

    public CommonOrder asCommonOrder() {
        String standName="";
        String brandName="";
        if(this.stand!= null){
            standName= this.stand.getName();
            brandName= this.stand.getBrandName();
        }

        return new CommonOrder(
                this.id,
                this.startTime,
                this.expectedTime,
                this.orderState,
                brandName,
                standName,
                this.orderItems.stream().map(OrderItem::asCommonOrderItem).collect(Collectors.toList()),
                this.latitude,
                this.longitude
        );


    }


    // ---- Getters and Setters ----- //

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(this);
    }

//    public String getBrandName() {
//        return this.stand.getBrandName();
//    }

    //public void setBrandName(String brandName) {
    //    this.brandName = brandName;
    //}

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
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
        expectedTime.add(Calendar.SECOND, remtime);
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public CommonOrder.State getOrderState() {
        return orderState;
    }

    public void setState(CommonOrder.State state) {
        this.orderState = state;
    }

//    public String getStandName() {
//        return standName;
//    }
//
//    public void setStandName(String standName) {
//        this.standName = standName;
//    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public boolean hasChosenStand() {
        return this.stand != null;
    }

    @Override
    public String toString() {
        return "Order{" +
                ", remainingTimeSec=" + this.computeRemainingTime() +
                ", orderStatus=" + orderState +
                ", orderItems=" + orderItems +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
