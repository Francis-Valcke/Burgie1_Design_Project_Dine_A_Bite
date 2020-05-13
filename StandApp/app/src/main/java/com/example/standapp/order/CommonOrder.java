package com.example.standapp.order;

import androidx.annotation.NonNull;

import com.example.standapp.json.CommonFood;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommonOrder implements Serializable {

    // unique id for this order
    private int id;

    private Calendar startTime;
    private Calendar expectedTime;

    private status orderState;

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

    public enum status {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }

    public CommonOrder() {}

    public CommonOrder(List<CommonFood> menuItems, String standName, String brandName,
                       double latitude, double longitude){
        this.id=0;
        this.latitude=latitude;
        this.longitude=longitude;

        this.standName=standName;
        this.brandName=brandName;

        this.startTime=Calendar.getInstance();
        this.expectedTime=Calendar.getInstance();

        this.orderState =status.SEND;

        this.orderItems=new ArrayList<>();
        for (CommonFood menuItem : menuItems) {
            orderItems.add(new CommonOrderItem(menuItem.getName(), menuItem.getCount(), menuItem.getPrice()));
        }
    }

    public int getId() {
        return id;
    }

    public int computeRemainingTime(){
        return (int) (expectedTime.getTimeInMillis()-startTime.getTimeInMillis());
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

    public status getOrderState() {
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

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = Calendar.getInstance();
        this.startTime.setTimeInMillis(startTime*100);
    }

    public Calendar getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        this.expectedTime = Calendar.getInstance();
        this.expectedTime.setTimeInMillis(expectedTime*100);
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