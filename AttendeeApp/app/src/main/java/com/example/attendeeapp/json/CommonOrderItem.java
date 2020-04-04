package com.example.attendeeapp.json;

public class CommonOrderItem {

    private String foodname;
    private int amount;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodname, int amount) {
        this.foodname = foodname;
        this.amount = amount;
    }

    public String getFoodname() {
        return foodname;
    }


    public int getAmount() {
        return amount;
    }


}
