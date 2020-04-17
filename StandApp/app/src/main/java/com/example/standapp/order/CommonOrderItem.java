package com.example.standapp.order;

import androidx.annotation.NonNull;

public class CommonOrderItem {

    private String foodName;
    private int amount;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodName, int amount){
        this.foodName = foodName;
        this.amount=amount;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getAmount() {
        return amount;
    }

    @NonNull
    @Override
    public String toString() {
        return "CommonOrderItem{" +
                "foodName='" + foodName + '\'' +
                ", amount=" + amount +
                '}';
    }
}
