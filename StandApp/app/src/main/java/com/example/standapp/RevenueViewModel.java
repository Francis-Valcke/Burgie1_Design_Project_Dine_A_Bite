package com.example.standapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.standapp.order.CommonOrderItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RevenueViewModel extends ViewModel {
    private MutableLiveData<BigDecimal> revenue = new MutableLiveData<>();
    private MutableLiveData<Map<String, BigDecimal>> prices = new MutableLiveData<>();

    public void updateRevenue(List<CommonOrderItem> items) {
        BigDecimal totalRevenue = revenue.getValue();
        for (CommonOrderItem item : items) {
            BigDecimal itemPrice = Objects.requireNonNull(Objects.requireNonNull(prices.getValue())
                    .get(item.getFoodName())).multiply(BigDecimal.valueOf(item.getAmount()));
            if (totalRevenue != null) totalRevenue = totalRevenue.add(itemPrice);
        }
        revenue.setValue(totalRevenue);
    }

    public LiveData<BigDecimal> getRevenue() {
        if (revenue.getValue() == null) {
            revenue.setValue(BigDecimal.ZERO);
        }
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue.setValue(revenue);
    }

    public void addPrice(String foodName, BigDecimal price) {
        Map<String, BigDecimal> map;
        if (prices.getValue() == null) {
            map = new HashMap<>();
        } else {
             map = prices.getValue();
        }
        if (map != null) map.put(foodName, price);
        prices.setValue(map);
    }

    public void editPrice(String foodName, BigDecimal price) {
        Map<String, BigDecimal> map = prices.getValue();
        if (map != null) {
            map.remove(foodName);
            map.put(foodName, price);
        }
    }

    public void deletePrice(String foodName) {
        Map<String, BigDecimal> map = prices.getValue();
        if (map != null) {
            map.remove(foodName);
        }
    }

    public void resetPrices() {
        this.prices = new MutableLiveData<>();
    }
}
