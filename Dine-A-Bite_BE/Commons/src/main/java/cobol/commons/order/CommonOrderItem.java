package cobol.commons.order;


public class CommonOrderItem {

    private String foodName;
    private int amount;

    public CommonOrderItem(){}

    public CommonOrderItem(String foodName, int amount){
        this.foodName = foodName;
        this.amount=amount;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getAmount() {
        return amount;
    }

}