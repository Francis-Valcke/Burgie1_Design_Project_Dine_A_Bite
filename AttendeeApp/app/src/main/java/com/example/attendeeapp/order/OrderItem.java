package com.example.attendeeapp.order;

public class OrderItem {

    private String foodname;
    private int amount;

    public OrderItem(String foodname, int amount) {
        this.foodname = foodname;
        this.amount = amount;
    }

    public String getFoodname() {
        return foodname;
    }

    public void setFoodname(String foodname) {
        this.foodname = foodname;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "foodname='" + foodname + '\'' +
                ", amount=" + amount +
                '}';
    }
}
