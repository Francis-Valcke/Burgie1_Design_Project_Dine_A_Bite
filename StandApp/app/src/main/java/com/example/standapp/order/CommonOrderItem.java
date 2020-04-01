package com.example.standapp.order;

public class CommonOrderItem {

    private int itemId;
    private String foodname; // TODO wrong naming convention
    private int amount;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodname, int amount) {
        this.foodname = foodname;
        this.amount = amount;
    }

    public int getItemId() {
        return itemId;
    }

    public String getFoodname() {
        return foodname;
    } // TODO wrong naming convention


    public int getAmount() {
        return amount;
    }

}
