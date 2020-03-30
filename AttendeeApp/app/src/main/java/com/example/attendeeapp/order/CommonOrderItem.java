package com.example.attendeeapp.order;

import java.io.Serializable;

public class CommonOrderItem implements Serializable {

    private String foodname;
    private int amount;
    private int itemId;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodname, int amount) {
        this.foodname = foodname;
        this.amount = amount;
    }

    public String getFoodname() {
        return foodname;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }


}
