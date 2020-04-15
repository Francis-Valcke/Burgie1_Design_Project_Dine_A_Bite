package cobol.commons.order;

import java.util.List;

public class SuperOrder {

    String brandName;
    List<CommonOrderItem> itemList;

    // Coordinates attendee on moment that order was made
    private double latitude;
    private double longitude;

    public SuperOrder(){

    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public List<CommonOrderItem> getItemList() {
        return itemList;
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

    public void setItemList(List<CommonOrderItem> itemList) {
        this.itemList = itemList;
    }
}
