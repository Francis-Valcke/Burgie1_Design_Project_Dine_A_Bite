package com.example.attendeeapp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Model for one menu item
 */
public class MenuItem implements Serializable {
    private static final int MAX_ITEM = 10;
    private String item;
    private BigDecimal price;
    private int count=0;

    public MenuItem(String item,BigDecimal price) {
        this.item = item;
        this.price = price.setScale(2,RoundingMode.HALF_UP);
        this.count = 0;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        return symbol + price.toString();
    }

    public int getCount(){
        return count;
    }

    public void increaseCount() throws ArithmeticException {
        if (this.count < MAX_ITEM) {
            this.count++;
        } else {
            throw new ArithmeticException("Overflow in menuItems (>MAX_ITEM)!");
        }
    }

    public void decreaseCount() {
        if(count == 0) return;
        this.count--;
    }

}
