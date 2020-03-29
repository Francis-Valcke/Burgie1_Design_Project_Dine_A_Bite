package com.example.attendeeapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class MenuFragmentGlobalTest {

    private MenuActivity menuActivity;
    private MenuFragmentGlobal menuFragmentGlobal;

    @Before
    public void setUp() {
        menuActivity = Robolectric.buildActivity(MenuActivity.class)
                .create().start().get();
        MenuFragmentAdapter adapter = new MenuFragmentAdapter(menuActivity);
        menuFragmentGlobal = (MenuFragmentGlobal) adapter.createFragment(0);
    }

    /**
     * Tests if menuFragmentGlobal launch is successful
     */
    @Test
    public void testLaunch() {
        // Fragment views cannot be accessed without helper methods!
        Assert.assertNotNull(menuFragmentGlobal);
    }

    /**
     * Test not possible for fragments
     */
    /*@Test
    public void testFetchMenu() {
        menuFragmentGlobal.fetchMenu("");
    }*/

}