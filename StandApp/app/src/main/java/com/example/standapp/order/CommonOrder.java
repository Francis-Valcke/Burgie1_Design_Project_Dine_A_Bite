package com.example.standapp.order;

import com.example.standapp.json.CommonFood;

import java.util.ArrayList;
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

    public CommonOrder() {}

    public CommonOrder(List<CommonFood> menuItems, String standName, String brandName, double latitude, double longitude){
        this.id=0;
        this.latitude=latitude;
        this.longitude=longitude;

        this.standName=standName;
        this.brandName=brandName;

        this.startTime=Calendar.getInstance();
        this.expectedTime=Calendar.getInstance();

        this.orderStatus=status.SEND;

        this.orderItems=new ArrayList<>();
        for (CommonFood menuItem : menuItems) {
            orderItems.add(new CommonOrderItem(menuItem.getName(), menuItem.getCount()));
        }
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