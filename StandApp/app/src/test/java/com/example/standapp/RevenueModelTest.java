package com.example.standapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.standapp.order.CommonOrderItem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RevenueModelTest {

    private RevenueViewModel mRevenueViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        mRevenueViewModel = new RevenueViewModel();
        mRevenueViewModel.setRevenue(new BigDecimal(0));
    }

    @Test
    public void testUpdateRevenue() {
        List<CommonOrderItem> order = new ArrayList<>();
        order.add(new CommonOrderItem("testFood", 5, new BigDecimal(10)));

        mRevenueViewModel.addPrice("testFood", new BigDecimal(10));
        mRevenueViewModel.updateRevenue(order);

        assertEquals(mRevenueViewModel.getRevenue().getValue(), new BigDecimal(50));
    }

}
