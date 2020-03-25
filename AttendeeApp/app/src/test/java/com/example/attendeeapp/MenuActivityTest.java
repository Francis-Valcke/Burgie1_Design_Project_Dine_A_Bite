package com.example.attendeeapp;

import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLooper;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MenuActivityTest {

    // TODO: test permission granting

    private MenuActivity menuActivity;

    @Before
    public void setUp() {
        menuActivity = Robolectric.buildActivity(MenuActivity.class)
                .create().start().get();
    }

    /**
     * Tests if menuActivity launch is successful
     */
    @Test
    public void testLaunch() {
        Assert.assertNotNull(menuActivity);
        RelativeLayout view = menuActivity.findViewById(R.id.activity_menu);
        assertNotNull(view);
    }

    /**
     * Tests if viewPager has 2 tabs, is showing the first one
     * and the FragmentAdapter is linked correctly
     */
    @Test
    public void testViewPager() {
        ViewPager2 viewPager = menuActivity.findViewById(R.id.menu_view_pager);
        assertNotNull(viewPager);

        RecyclerView.Adapter pager_adapter = viewPager.getAdapter();
        assertTrue(pager_adapter instanceof MenuFragmentAdapter);

        // Check if the viewpager has 2 tabs and is showing the first one
        assertEquals(2, pager_adapter.getItemCount());
        assertEquals(0, viewPager.getCurrentItem());
    }

    /**
     * Tests if tabLayout has 2 tabs, with the appropriate display text
     * and is showing the first one
     */
    @Test
    public void testTabLayout() {
        TabLayout tab = menuActivity.findViewById(R.id.menu_tab_layout);
        assertNotNull(tab);

        // Check if the tabLayout has 2 tabs and is showing the first one
        assertEquals(2, tab.getTabCount());
        assertEquals(menuActivity.getString(R.string.tab_global), tab.getTabAt(0).getText());
        assertEquals(menuActivity.getString(R.string.tab_stand), tab.getTabAt(1).getText());
        assertEquals(0, tab.getSelectedTabPosition());
    }

    /**
     * Tests if cartLayout is clickable
     * and if the initial cartCount = 0
     * TODO: check updated cartText
     */
    @Test
    public void testCartLayout() {
        RelativeLayout cart_layout = menuActivity.findViewById(R.id.cart_layout);
        assertNotNull(cart_layout);

        assertTrue(cart_layout.isClickable());

        TextView cartCount = menuActivity.findViewById(R.id.cart_count);
        assertNotNull(cartCount);
        assertEquals("0", cartCount.getText());
    }

    /**
     * Tests if the toolbar is initialized correctly
     */
    @Test
    public void testToolbar() {
        Toolbar toolbar = menuActivity.findViewById(R.id.toolbar);
        assertNotNull(toolbar);

        // Further checks for toolbar
        //assertEquals(toolbar.getTabCount(), 2);
    }

    /**
     * Test if cartActivity is launched correctly
     * and if cartList is successfully passed when clicked
     */
    @Test
    public void LaunchingNextActivity() {
        ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();
        MenuItem i = new MenuItem("food", new BigDecimal(0), "");
        cartList.add(i);
        menuActivity.onCartChangedAdd(i);

        // Perform click
        RelativeLayout cart_layout = menuActivity.findViewById(R.id.cart_layout);
        cart_layout.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Intent startedIntent = shadowOf(menuActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(CartActivity.class, shadowIntent.getIntentClass());
        assertEquals(cartList, startedIntent.getSerializableExtra("cartList"));
    }


    /*
            // Random string
                byte[] array = new byte[10]; // length is bounded by 20
            new Random().nextBytes(array);
            String randomBrand = new String(array, StandardCharsets.UTF_8);

     */

    /**
     * Testing behavior when adding and removing items from the cart
     * Test 1:
     * Adding 30 items: 2 different item 15 times, (same food, same brand, and different stand name)
     * Removing some items that were not in the cart (different food/stand/brand names)
     * Removing the same items in reserved order
     */
    @Test
    public void testOnCartChanged_1() {
        TextView cartCountText = menuActivity.findViewById(R.id.cart_count);
        int cartCount = 0;
        ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();

        // Adding the items, same food, same brand, different price, different stand
        int testCount = 0;
        for (int i = 0; i < 30; i++) {
            MenuItem m = new MenuItem("food", new BigDecimal(5.51 * ((int)(i/15) + 1)),
                    "brand");
            if( i/15 == 0 ) m.setStandName("stand0");
            cartList.add(m);

            cartCount = menuActivity.onCartChangedAdd(cartList.get(i));

            // Only 20 can be added, 10 of each
            if (!(i >= 10 && i < 15 || i >= 25)) {
                testCount += 1;
            }
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

        // Remove some items that are not in the cart (different food/stand/brand names), random price
        String[] food = {"foodX", "food0", "foodX"};
        String[] stand = {"stand0", "standX", "standX"};
        String[] brand = {"brand0", "brandX"};
        testCount = 20;

        for (int i = 0; i < food.length*brand.length; i++) {
            MenuItem m = new MenuItem(food[i%food.length], new BigDecimal(Math.random()*100),
                    brand[i/(food.length*brand.length)]);
            m.setStandName(stand[i%stand.length]);

            cartCount = menuActivity.onCartChangedRemove(m);

            // Cart should not be altered, testCount stays unchanged
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

        // Removing the items in reversed order
        testCount = 20;
        for (int i = 29; i > 0; i--) {
            cartCount = menuActivity.onCartChangedRemove(cartList.get(i));

            // Only 20 can be removed, 10 of each
            if (!(i >= 15 && i < 20 || i < 5)) {
                testCount -= 1;
            }

            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

    }

    /**
     * Testing behavior when adding and removing items from the cart
     * Test 2:
     * Adding 30 items: 3 different item 10 times, (same food, different brand, and same stand name)
     * Removing the same items
     */
    @Test
    public void testOnCartChanged_2() {
        TextView cartCountText = menuActivity.findViewById(R.id.cart_count);
        int cartCount = 0;
        ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();

        // Adding the items, same food, different brand, different price, same stand
        for (int i = 0; i < 30; i++) {
            MenuItem m = new MenuItem("food", new BigDecimal(5.51 * (i%3 + 1)),
                    "brand" + i%3);
            m.setStandName("stand");
            cartList.add(m);

            cartCount = menuActivity.onCartChangedAdd(cartList.get(i));

            // Only 25 can be added, max 10 of each
            int testCount = i + 1;
            if (i >= 25) testCount = 25;
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

        // Removing the items
        for (int i = 0; i < 30; i++) {
            cartCount = menuActivity.onCartChangedRemove(cartList.get(i));

            // Only 25 can be removed
            int testCount = 25 - i - 1;
            if (i >= 25) testCount = 0;  // if all are removed, cart stays empty
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

    }

    /**
     * Testing behavior when adding and removing items from the cart
     * Test 3:
     * Adding 30 items: 3 different item 10 times, (different food, same brand, and same stand name)
     * Removing the same items
     */
    @Test
    public void testOnCartChanged_3() {
        TextView cartCountText = menuActivity.findViewById(R.id.cart_count);
        int cartCount = 0;
        ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();

        // Adding the items, different food, same brand, same price, same stand (global="")
        for (int i = 0; i < 30; i++) {
            MenuItem m = new MenuItem("food" + i%3, new BigDecimal(5.51),
                    "brand");
            cartList.add(m);

            cartCount = menuActivity.onCartChangedAdd(cartList.get(i));

            // Only 25 can be added, max 10 of each
            int testCount = i + 1;
            if (i >= 25) testCount = 25;
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

        // Removing the items
        for (int i = 0; i < 30; i++) {
            cartCount = menuActivity.onCartChangedRemove(cartList.get(i));

            // Only 25 can be removed
            int testCount = 25 - i - 1;
            if (i >= 25) testCount = 0;  // if all are removed, cart stays empty
            assertEquals(testCount, cartCount);
            assertEquals("" + testCount, cartCountText.getText());
        }

    }

}