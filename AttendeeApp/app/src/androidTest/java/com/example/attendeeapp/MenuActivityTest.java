package com.example.attendeeapp;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static org.junit.Assert.*;

public class MenuActivityTest {

    @Rule
    public ActivityTestRule<MenuActivity> rule =
            new ActivityTestRule<MenuActivity>(MenuActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule
            .grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    private MenuActivity mActivity = null;

    @Before
    public void setUp() throws Exception {
        mActivity = rule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
        mActivity = null;
    }

    /**
     * Tests if menuActivity launch is successful
     */
    @Test
    public void testLaunch() {
        View view_layout = mActivity.findViewById(R.id.activity_menu);
        assertNotNull(view_layout);
        assertTrue(view_layout instanceof RelativeLayout);
    }

    /**
     * Tests if viewPager has 2 tabs, is showing the first one
     * and the FragmentAdapter is linked correctly
     */
    @Test
    public void testViewPager() {
        View view_pager = mActivity.findViewById(R.id.menu_view_pager);
        assertNotNull(view_pager);
        assertTrue(view_pager instanceof ViewPager2);
        ViewPager2 viewPager = (ViewPager2) view_pager;

        Adapter pager_adapter = viewPager.getAdapter();
        assertTrue(viewPager.getAdapter() instanceof MenuFragmentAdapter);

        // Check if the viewpager has 2 tabs and is showing the first one
        assertEquals(pager_adapter.getItemCount(), 2);
        assertEquals(viewPager.getCurrentItem(), 0);
    }

    /**
     * Tests if tabLayout has 2 tabs, with the appropriate display text
     * and is showing the first one
     */
    @Test
    public void testTabLayout() {
        View view_tab = mActivity.findViewById(R.id.menu_tab_layout);
        assertNotNull(view_tab);
        assertTrue(view_tab instanceof TabLayout);
        TabLayout tab = (TabLayout) view_tab;

        // Check if the tabLayout has 2 tabs and is showing the first one
        assertEquals(tab.getTabCount(), 2);
        assertEquals(tab.getTabAt(0).getText(), mActivity.getString(R.string.tab_global));
        assertEquals(tab.getTabAt(1).getText(), mActivity.getString(R.string.tab_stand));
        assertEquals(tab.getSelectedTabPosition(), 0);
    }

    /**
     * Tests if cartLayout is clickable
     * and if the initial cartCount = 0
     */
    @Test
    public void testCartLayout() {
        View view_tab = mActivity.findViewById(R.id.cart_layout);
        assertNotNull(view_tab);
        assertTrue(view_tab instanceof RelativeLayout);
        RelativeLayout cart_layout = (RelativeLayout) view_tab;

        assertTrue(cart_layout.isClickable());

        assertTrue(mActivity.findViewById(R.id.cart_count) instanceof TextView);
        TextView cartCount = mActivity.findViewById(R.id.cart_count);

        assertEquals(cartCount.getText(), "0");
    }

    /**
     * Tests if the toolbar is initialized correctly
     */
    @Test
    public void testToolbar() {
        View view_bar = mActivity.findViewById(R.id.toolbar);
        assertNotNull(view_bar);
        /*assertTrue(view_bar instanceof Toolbar);
        Toolbar toolbar = (Toolbar) view_bar;*/

        // Further checks for toolbar
        /*assertEquals(tab.getTabCount(), 2);;*/
    }

    /**
     * Testing behavior when adding items to your cart
     * Test 1: adding 2 items 15 times
     * TODO: add test for removing items from cart
     */
    @UiThreadTest
    @Test
    public void testOnCartChanged_1() {
        int cartCount = 0;
        for (int i = 0; i < 30; i++){
            String s = "food" + i%2;
            cartCount = mActivity.onCartChangedAdd(new MenuItem(s,
                    new BigDecimal(Math.random()*100), ""));

            // Only 10 of each should be added
            int a = i + 1;
            if (i >= 20) a = 20;
            assertEquals(cartCount, a);
        }
    }

    /**
     * Testing behavior when adding items to your cart
     * Test 2: adding the same item 15 times
     * then adding another 2 items 15 times
     */
    @UiThreadTest
    @Test
    public void testOnCartChanged_2() {
        // Adding the same item 15 times
        int cartCount = 0;
        int b = 0;
        for (int i = 0; i < 15; i++){
            String s = "food";
            cartCount = mActivity.onCartChangedAdd(new MenuItem(s,
                    new BigDecimal(Math.random()*100), ""));

            // Only 10 items maximum
            b = i + 1;
            if (i >= 10) b = 10;
            assertEquals(cartCount, b);
        }

        // Then adding 2 different items another 15 times
        for (int i = 0; i < 30; i++){
            String s = "food" + i%2;
            cartCount = mActivity.onCartChangedAdd(new MenuItem(s,
                    new BigDecimal(Math.random()*100), ""));

            // Only 25 total items maximum
            int a = i + b + 1;
            if (i >= 25 - b) a = 25;
            assertEquals(cartCount, a);
        }
    }

    /**
     * Testing behavior when adding items to your cart
     * Test 3: adding 30 different items
     * while continuously checking cartCount
     */
    @UiThreadTest
    @Test
    public void testOnCartChanged_3() {
        int cartCount = 0;
        for (int i = 0; i < 30; i++){
            String s = "food" + i;
            cartCount = mActivity.onCartChangedAdd(new MenuItem(s,
                    new BigDecimal(Math.random()*100), ""));

            // Only 25 total items maximum
            int a = i + 1;
            if (i >= 25) a = 25;
            assertEquals(cartCount, a);
        }
    }

    /**
     * Test if cartActivity is launched correctly
     * and if cartList is successfully passed
     */
    @Test
    public void testNextActivity() {
        ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();
        final MenuItem i = new MenuItem("food", new BigDecimal(0), "");
        cartList.add(i);

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.onCartChangedAdd(i);
                }
            });
        } catch (Throwable e) {
            Log.v("TestNextActivityError", e.toString());
        }

        Intents.init();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.cart_layout)).perform(click());
        intended(hasComponent(CartActivity.class.getName()));
        intended(hasExtra("cartList", cartList));
        Intents.release();
    }

}