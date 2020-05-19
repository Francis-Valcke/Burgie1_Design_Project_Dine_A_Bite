package com.example.standapp.order;

import androidx.annotation.NonNull;

import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.threeten.bp.Duration;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommonOrder implements Serializable {

    // unique id for this order
    private int id;

    @JsonDeserialize(using= ZonedDateTimeDeserializer.class)
    private ZonedDateTime startTime;
    @JsonDeserialize(using= ZonedDateTimeDeserializer.class)
    private ZonedDateTime expectedTime;

    private State orderState;

    private int standId;
    private String brandName;
    private String standName;

    private BigDecimal totalPrice;
    private int totalCount;
    //----- Request ------//
    private List<CommonOrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    private double latitude;
    private double longitude;

    private RecommendType recType;

    public enum State {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY, // DONE
        PICKED_UP,
        BEGUN;// START
    }

    // Type of recommendation wanted (not used in Stand app)
    public enum RecommendType {
        DISTANCE,
        TIME,
        DISTANCE_AND_TIME
    }

    public CommonOrder() {}

    public CommonOrder(List<CommonFood> menuItems, String standName, String brandName,
                       double latitude, double longitude, RecommendType recType){
        this.id=0;
        this.latitude=latitude;
        this.longitude=longitude;

        this.standName=standName;
        this.brandName=brandName;
        this.startTime=ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        this.expectedTime=ZonedDateTime.now(ZoneId.of("Europe/Brussels"));

        this.orderState = State.SEND;
        this.recType = recType;

        this.orderItems=new ArrayList<>();
        for (CommonFood menuItem : menuItems) {
            orderItems.add(new CommonOrderItem(menuItem.getName(), menuItem.getCount(), menuItem.getPrice()));
        }
    }

    public int getId() {
        return id;
    }

    public int computeRemainingTime(){
        return (int) Duration.between(startTime, expectedTime).toMillis();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public State getOrderState() {
        return orderState;
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

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(ZonedDateTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public RecommendType getRecType() {
        return recType;
    }

    public void setRecType(RecommendType recType) {
        this.recType = recType;
    }

    @NonNull
    @Override
    public String toString() {
        return "CommonOrder{" +
                "id=" + id +
                //", startTime=" + startTime +
                //", expectedTime=" + expectedTime +
                ", orderState=" + orderState +
                ", standId=" + standId +
                ", brandName='" + brandName + '\'' +
                ", standName='" + standName + '\'' +
                ", orderItems=" + orderItems +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}