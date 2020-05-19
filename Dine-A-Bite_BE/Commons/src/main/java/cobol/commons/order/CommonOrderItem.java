package cobol.commons.order;


import java.math.BigDecimal;

/**
 * Item (order consists of list of items) class used to represent order items between different components
 */

public class CommonOrderItem {


    private String foodName;
    private int amount;
    private BigDecimal price;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodName, int amount, BigDecimal price){
        this.foodName = foodName;
        this.amount=amount;
        this.price=price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}