package cobol.commons.order;

import java.util.List;

public class SuperOrder {

    String brandName;
    List<CommonOrderItem> orderItems;

    // Coordinates attendee on moment that order was made
    private double latitude;
    private double longitude;
    private CommonOrder.RecommendType recType;

    public SuperOrder(){

    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public List<CommonOrderItem> getOrderItems() {
        return orderItems;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setOrderItems(List<CommonOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public CommonOrder.RecommendType getRecType() {
        return recType;
    }

    public void setRecType(CommonOrder.RecommendType recType) {
        this.recType = recType;
    }
}
