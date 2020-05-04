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
    private MutableLiveData<BigDecimal> revenue;
    private MutableLiveData<Map<String, BigDecimal>> prices;

    public void updateRevenue(List<CommonOrderItem> items) {
        BigDecimal totalRevenue = revenue.getValue();
        for (CommonOrderItem item : items) {
            BigDecimal itemPrice = Objects.requireNonNull(Objects.requireNonNull(prices.getValue()).get(item.getFoodName())).multiply(BigDecimal.valueOf(item.getAmount()));
            totalRevenue = totalRevenue.add(itemPrice);
        }
        revenue.setValue(totalRevenue);
    }

    public LiveData<BigDecimal> getRevenue() {
        if (revenue == null) {
            revenue = new MutableLiveData<>();
            revenue.setValue(BigDecimal.ZERO);
        }
        return revenue;
    }

    public void addPrice(String foodName, BigDecimal price) {
        Map<String, BigDecimal> map;
        if (prices == null) {
            map = new HashMap<>();
            prices = new MutableLiveData<>();
        } else {
             map = prices.getValue();
        }
        map.put(foodName, price);
        prices.setValue(map);
    }
}
