package cobol.commons.order;

import java.util.List;

public class SuperOrder {

    String brandName;
    List<CommonOrder> itemList;

    public SuperOrder(){

    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public List<CommonOrder> getItemList() {
        return itemList;
    }

    public void setItemList(List<CommonOrder> itemList) {
        this.itemList = itemList;
    }
}
