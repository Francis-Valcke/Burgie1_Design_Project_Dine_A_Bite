package com.example.attendeeapp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;

/**
 * Model for one menu item
 */
public class MenuItem implements Serializable {
    private static final int MAX_ITEM = 10;
    private String foodName;
    private BigDecimal price;
    private int count=0;

    private String standName;
    private String brandName;
    HashSet<String> category = new HashSet<String>();
    String description;

    public MenuItem(String foodName, BigDecimal price, String brandName) {
        this.foodName = foodName;
        this.price = price.setScale(2,RoundingMode.HALF_UP);
        this.count = 0;
        this.brandName = brandName;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public HashSet<String> getCategory() {
        return category;
    }

    /**
     * Will only add distinct categories (set)
     * @param cat: Category to add
     */
    public void addCategory(String cat) {
        category.add(cat);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Return the price of a menu item with the euro symbol
     * @return: String of euro symbol with price
     */
    public String getPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        return symbol + price.toString();
    }

    public int getCount(){
        return count;
    }

    /**
     * Increases the number of times the item is added to the cart
     * Throws ArithmeticException when the maximum number of this item is reached
     */
    public void increaseCount() throws ArithmeticException {
        if (this.count < MAX_ITEM) {
            this.count++;
        } else {
            throw new ArithmeticException("Overflow in menuItems (>MAX_ITEM)!");
        }
    }

    /**
     * Decreases the number of times the item is in the cart
     * Throws ArithmeticException when this item has reached 0
     */
    public void decreaseCount() throws ArithmeticException{
        if(count == 0) throw new ArithmeticException("This menuItem cannot be decreased!");
        this.count--;
    }

}
