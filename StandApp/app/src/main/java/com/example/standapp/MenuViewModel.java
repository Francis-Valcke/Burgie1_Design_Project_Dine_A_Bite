package com.example.standapp;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.standapp.json.CommonFood;
import com.example.standapp.order.CommonOrderItem;

import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel {

    private MutableLiveData<List<CommonFood>> menuList;

    public LiveData<List<CommonFood>> getMenuList() {
        return menuList;
    }

    public void loadMenuList() {
        // see fetchMenu in ProfileFragment
    }

    /**
     * Decrease the current stock values of the menu items of the stand
     * based on incoming orders
     * @param orderItems: menu items of stand that are being ordered
     */
    @SuppressWarnings("unchecked")
    public void decreaseStock(List<CommonOrderItem> orderItems) {
        for (CommonOrderItem orderItem : orderItems) {
            for (CommonFood menuItem : menuList) {
                if (orderItem.getFoodName().equals(menuItem.getName())) {
                    menuItem.decreaseStock(orderItem.getAmount());
                }
            }
            
        }
    }
}
