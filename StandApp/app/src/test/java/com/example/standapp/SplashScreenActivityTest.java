package com.example.standapp;

import android.content.Intent;
import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SplashScreenActivityTest {

    private SplashScreenActivity splashScreenActivity;

    @Before
    public void setUp() {
        splashScreenActivity = Robolectric.buildActivity( SplashScreenActivity.class )
                .create().start().get();
    }

    /**
     * Tests if splashScreenActivity launch is successful
     */
    @Test
    public void testLaunch() {
        Assert.assertNotNull(splashScreenActivity);
        //LinearLayout view = splashScreenActivity.findViewById(R.id.nav_profile);
        //assertNotNull(view);
    }

    /**
     * Tests if splashScreenActivity launches mainActivity
     */
    @Test
    public void isLaunchingNext() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Intent startedIntent = shadowOf(splashScreenActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MainActivity.class, shadowIntent.getIntentClass());
    }

}