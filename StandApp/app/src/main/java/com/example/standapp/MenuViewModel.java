package com.example.standapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.standapp.json.CommonFood;
import com.example.standapp.order.CommonOrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel class for the list of menu items of the logged in stand
 */
public class MenuViewModel extends ViewModel {

    private MutableLiveData<ArrayList<CommonFood>> menuList = new MutableLiveData<>();

    public LiveData<ArrayList<CommonFood>> getLiveDataMenuList() {
        return menuList;
    }

    public void setMenuList(ArrayList<CommonFood> menuList) {
        this.menuList.setValue(menuList);
    }

    /**
     * Get the list of menu items of the logged in stand
     *
     * @return list of CommonFood objects representing the menu items of the stand
     */
    public ArrayList<CommonFood> getMenuList() {
        return menuList.getValue();
    }

    /**
     * Clear the saved menu list in the app
     * (e.g. when there is a stand change)
     */
    public void resetMenuList() {
        ArrayList<CommonFood> items = menuList.getValue();
        if (items != null) {
            items.clear();
        } else {
            menuList.setValue(new ArrayList<CommonFood>());
        }
    }

    /**
     * Load/fetch the menu list from the server of the logged in stand
     * Not implemented, see fetchMenu in ProfileFragment
     */
    public void loadMenuList() {
        // see fetchMenu in ProfileFragment
        // could be copied to here
    }

    public void addMenuItem(CommonFood item) {
        ArrayList<CommonFood> items = menuList.getValue();
        if (items != null) items.add(item);
        menuList.setValue(items);
    }

    public void editMenuItem(CommonFood item, int position) {
        ArrayList<CommonFood> items = menuList.getValue();
        if (items != null) items.set(position, item);
        menuList.setValue(items);
    }

    public void deleteMenuItem(int position) {
        ArrayList<CommonFood> items = menuList.getValue();
        if (items != null)items.remove(position);
        menuList.setValue(items);
    }

    /**
     * Decrease the current stock values of the menu items of the stand
     * based on incoming orders
     *
     * @param orderItems menu items of stand that are being ordered
     */
    public void decreaseStock(List<CommonOrderItem> orderItems) {
        for (CommonOrderItem orderItem : orderItems) {
            ArrayList<CommonFood> items = menuList.getValue();
            for (CommonFood item : Objects.requireNonNull(items)) {
                if (orderItem.getFoodName().equals(item.getName())) {
                    item.decreaseStock(orderItem.getAmount());
                }
            }
        }
    }
}
