package com.example.standapp;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

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
    }

    /**
     * Tests if splashScreenActivity launches mainActivity
     *
     * https://github.com/robolectric/robolectric/issues/1518
     */
    @Ignore("Literally impossible to test an activity using Android Keystore")
    public void isLaunchingNext() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Intent startedIntent = shadowOf(splashScreenActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MainActivity.class, shadowIntent.getIntentClass());
    }

}