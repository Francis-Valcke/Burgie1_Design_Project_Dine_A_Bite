package com.example.standapp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Model for one menu item
 */
public class MenuItem implements Serializable {

    private static final int MAX_ITEM = 10;
    private String foodName;
    private String standName = "";
    private String brandName;
    private String description = "";
    private BigDecimal price;
    private int preptime;
    private int stock;
    private int count = 0;
    private List<String> category = new ArrayList<>();

    public MenuItem(){
        // needed for ObjectMapper
        super();
    }

    public MenuItem(String foodName, BigDecimal price, int preptime, int stock, String brandName, String desc, List<String> category) {
        this.foodName = foodName;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.preptime = preptime;
        this.stock = stock;
        this.brandName = brandName;
        this.description = desc;
        this.category = category;
        this.count = 0;
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

    public List<String> getCategory() {
        return category;
    }

    /**
     * Will only add distinct categories (set)
     * @param cat: Category to add
     * @return if the add was successful
     */
    public boolean addCategory(String cat) {
        return category.add(cat);
    }

    public boolean removeCategory(String cat) {
        return category.remove(cat);
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
     * @return String of euro symbol with price
     */
    public String getPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = Objects.requireNonNull(euro.getCurrency()).getSymbol();
        return symbol + price.toString();
    }

    public int getPreptime() {
        return preptime;
    }

    public void setPreptime(int preptime) {
        this.preptime = preptime;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCount(){
        return count;
    }
}
