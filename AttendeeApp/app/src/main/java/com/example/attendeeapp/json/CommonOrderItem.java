package com.example.attendeeapp.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Model for one ordered item.
 */
public class CommonOrderItem implements Serializable {

    private String foodName;
    private int amount;
    private BigDecimal price;

    public CommonOrderItem() {}

    public CommonOrderItem(String foodName, int amount, BigDecimal price) {
        this.foodName = foodName;
        this.amount = amount;
        this.price = price;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Return the price of an order item with the euro symbol.
     * @return String of euro symbol and the order item price.
     */
    @JsonIgnore
    public String getPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();

        if(price!=null){
            return symbol + " " + price.toString();
        }
        else{
            return symbol + " NA";
        }
    }


}
