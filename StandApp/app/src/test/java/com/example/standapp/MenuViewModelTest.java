package com.example.standapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.standapp.json.CommonFood;
import com.example.standapp.order.CommonOrderItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// https://jeroenmols.com/blog/2019/01/17/livedatajunit5/

public class MenuViewModelTest {

    private MenuViewModel mMenuViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        mMenuViewModel = new MenuViewModel();
        ArrayList<CommonFood> menuList = new ArrayList<>();
        mMenuViewModel.setMenuList(menuList);
    }

    @After
    public void cleanUp() {
        ArrayList<CommonFood> menuList = new ArrayList<>();
        mMenuViewModel.setMenuList(menuList);
    }

    @Test
    public void testAddMenuItem() {
        CommonFood testFood = new CommonFood("testFood", new BigDecimal(10),
                "testBrand");
        mMenuViewModel.addMenuItem(testFood);

        assertEquals(mMenuViewModel.getMenuList().get(0),
                testFood);
    }

    @Test
    public void testEditMenuItem() {
        CommonFood testFood = new CommonFood("testFood", new BigDecimal(10),
                "testBrand");
        CommonFood editTestFood = new CommonFood("editTestFood", new BigDecimal(20),
                "testBrand");
        mMenuViewModel.addMenuItem(testFood);
        mMenuViewModel.editMenuItem(editTestFood, 0);

        assertEquals(mMenuViewModel.getMenuList().get(0),
                editTestFood);
    }

    @Test
    public void testDeleteMenuItem() {
        CommonFood testFood = new CommonFood("testFood", new BigDecimal(10),
                "testBrand");
        mMenuViewModel.addMenuItem(testFood);
        mMenuViewModel.deleteMenuItem(0);

        assertTrue(mMenuViewModel.getMenuList().isEmpty());
    }

    @Test
    public void testDecreaseStock() {
        CommonFood testFood = new CommonFood("testFood", new BigDecimal(10),
                "testBrand");
        testFood.setStock(10);
        mMenuViewModel.addMenuItem(testFood);

        List<CommonOrderItem> order = new ArrayList<>();
        order.add(new CommonOrderItem("testFood", 5, new BigDecimal(10)));
        mMenuViewModel.decreaseStock(order);

        assertEquals(mMenuViewModel.getMenuList().get(0).getStock(),
                5);
    }
}
