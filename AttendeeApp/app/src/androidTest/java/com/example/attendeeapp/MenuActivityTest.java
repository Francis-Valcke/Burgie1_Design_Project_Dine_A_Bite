package com.example.attendeeapp;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.test.rule.ActivityTestRule;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class MenuActivityTest {

    @Rule
    public ActivityTestRule<MenuActivity> rule =
            new ActivityTestRule<MenuActivity>(MenuActivity.class);

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

    @Test
    public void testOnCartChanged() {
        // Test behavior when a menu item is added to the cart
    }

}