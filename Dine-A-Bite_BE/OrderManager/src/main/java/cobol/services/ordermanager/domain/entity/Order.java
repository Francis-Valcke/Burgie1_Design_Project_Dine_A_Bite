package cobol.services.ordermanager.domain.entity;

import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.Modifying;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private ZonedDateTime startTime;
    @Column(columnDefinition = "datetime")
    private ZonedDateTime expectedTime;
    @Column
    private CommonOrder.State orderState;
    // Coordinates Attendee on moment that order was mad
    @Column
    private double latitude;
    @Column
    private double longitude;

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
    private CommonOrder.recommendType recType;


    // ---- Constructor / Transformers ---- //

    /**
     * Default empty constructor
     */
    public Order() {
    }

    /**
     * will transform CommonOrder object to Order object
     * used when receiving an order from AttendeeApp
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
        this.orderState = orderObject.getOrderState();
        this.startTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        this.expectedTime = ZonedDateTime.from(startTime);
        this.recType = orderObject.getRecType();

    }

    /**
     * will transform Order object to Commonorder object
     * Used to send
     *  - an order to the AttendeeApp/StandApp
     *  - an order to the StandManager in order to receive a recommendation
     *
     * @return CommonOrder object
     */
    public CommonOrder asCommonOrder() {
        String standName = "";
        String brandName = "";
        if (this.stand != null) {
            standName = this.stand.getName();
            brandName = this.stand.getBrandName();
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
                this.longitude,
                this.recType
        );


    }


    // ---- Update / Compute ---- //

    /**
     * Will update the expected time based on remainingTime
     *
     * @param remainingTime time in seconds
     */
    public void setRemtime(int remainingTime) {
        expectedTime = expectedTime.plusSeconds(remainingTime);
    }


    /**
     * Computes remaining time till expected time with respect to current time
     * @return RemainingTime in seconds
     */
    public int computeRemainingTime() {
        ZonedDateTime actual = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        if (actual.isAfter(expectedTime)){
            return 0;
        }
        else {
            Duration remaining = Duration.between(actual, expectedTime);
            return (int) remaining.getSeconds();
        }
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

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Stand getStand() {
        return stand;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
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
