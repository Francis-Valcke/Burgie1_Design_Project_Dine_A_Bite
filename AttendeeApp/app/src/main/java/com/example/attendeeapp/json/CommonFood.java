package com.example.attendeeapp.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model for one menu item
 */
public class CommonFood implements Serializable {

    private static final int MAX_ITEM = 10;
    private String name;
    private BigDecimal price;
    private int count = 0;
    private int preparationTime;
    private int stock;
    private String standName = "";
    private String brandName;
    private List<String> category = new ArrayList<>();
    private String description = "";

    public CommonFood() {
        super();//needed for ObjectMapper
    }

    public CommonFood(String name, BigDecimal price, int preparationTime, int stock,
                      String brandName, String desc, List<String> category) {
        this.name = name;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.preparationTime = preparationTime;
        this.stock = stock;
        this.brandName = brandName;
        this.description = desc;
        this.category = category;
        this.count = 0;
    }

    public CommonFood(String foodName, BigDecimal price, int preparationTime, int stock,
                      String standName, String brandName, String desc, List<String> category) {
        this.name = foodName;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.preparationTime = preparationTime;
        this.stock = stock;
        this.count = 0;
        this.standName = standName;
        this.brandName = brandName;
        this.description = desc;
        this.category = category;
    }

    public CommonFood(CommonFood copy) {
        this.name = copy.name;
        this.price = copy.getPrice();
        this.count = copy.count;
        this.standName = copy.standName;
        this.brandName = copy.brandName;
        this.category = new ArrayList<>(copy.category);
        this.description = copy.description;
    }

    public CommonFood(String foodName, BigDecimal price, String brandName) {
        this.name = foodName;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.brandName = brandName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
     *
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
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Return the price of a menu item with the euro symbol
     *
     * @return String of euro symbol with price
     */
    @JsonIgnore
    public String getPriceEuro() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        return symbol + " " + price.toString();
    }

    public int getCount() {
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
    public void decreaseCount() throws ArithmeticException {
        if (count == 0) throw new ArithmeticException("This menuItem cannot be decreased!");
        this.count--;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}