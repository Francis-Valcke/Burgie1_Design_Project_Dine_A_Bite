package com.example.attendeeapp;

import android.content.Intent;
import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        mainActivity = Robolectric.buildActivity( MainActivity.class )
                .create().start().get();
    }

    /**
     * Tests if main activity launch is successful
     */
    @Test
    public void testLaunch() {
        Assert.assertNotNull(mainActivity);
        LinearLayout view = mainActivity.findViewById(R.id.activity_main);
        assertNotNull(view);
    }

    /**
     * Tests if mainActivity launches menu activity
     */
    @Test
    public void isLaunchingNext() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Intent startedIntent = shadowOf(mainActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MenuActivity.class, shadowIntent.getIntentClass());
    }

}