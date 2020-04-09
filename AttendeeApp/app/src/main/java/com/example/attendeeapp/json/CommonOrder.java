package com.example.attendeeapp.json;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.attendeeapp.appDatabase.Converters;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Entity
public class CommonOrder implements Serializable {

    // unique id for this order
    @PrimaryKey
    private int id;

    @TypeConverters(Converters.class)
    private Calendar startTime;
    @TypeConverters(Converters.class)
    private Calendar expectedTime;

    @TypeConverters(Converters.class)
    private State orderState;

    private int standId;
    private String brandName;
    private String standName;

    //----- Request ------//
    @TypeConverters(Converters.class)
    private List<CommonOrderItem> orderItems;

    // Coordinates Attendee on moment that order was mad
    private double latitude;
    private double longitude;

    @JsonIgnore
    @TypeConverters(Converters.class)
    private BigDecimal totalPrice;

    @JsonIgnore
    private int totalCount;

    public enum State {
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

        this.orderState=State.SEND;

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

    public void setId(int id) {
        this.id = id;
    }

    public void setOrderState(State orderState) {
        this.orderState = orderState;
    }

    public void setStandId(int standId) {
        this.standId = standId;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public void setOrderItems(List<CommonOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(Calendar expectedTime) {
        this.expectedTime = expectedTime;
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

    /**
     * Return the total price of the order with the euro symbol
     * @return String of euro symbol with total price
     */
    @JsonIgnore
    public String getTotalPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        return symbol + " " + totalPrice.toString();
    }

    /**
     * Helper function that updates the CommonOrderItem prices, because server doesn't send them
     * @param list: the items to get the price from
     */
    public void setPrices(ArrayList<CommonFood> list) {
        for(CommonFood menuItem : list)  {
            for(CommonOrderItem item : orderItems) {
                if(item.getFoodName().equals(menuItem.getName())) {
                    item.setPrice(menuItem.getPrice());
                }

            }
        }
    }

}