package com.example.attendeeapp;

import android.content.Intent;
import android.widget.LinearLayout;

import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.ui.login.LoginActivity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Test written to unit test the MainActivity functionality.
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        mainActivity = Robolectric.buildActivity( MainActivity.class )
                .create().start().get();
    }

    /**
     * Tests if the main activity launch is successful.
     */
    @Test
    public void testLaunch() {
        Assert.assertNotNull(mainActivity);
        LinearLayout view = mainActivity.findViewById(R.id.activity_main);
        assertNotNull(view);
    }

    /**
     * Tests if MainActivity launches LoginActivity when not logged in.
     */
    @Ignore("Problem with Android Keystore used in MainActivity")
    @Test
    public void isLaunchingNextNotLoggedIn() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Intent startedIntent = shadowOf(mainActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(LoginActivity.class, shadowIntent.getIntentClass());
    }

    /**
     * Tests if MainActivity launches MenuActivity when logged in.
     */
    @Ignore("Problem with Android Keystore used in MainActivity")
    @Test
    public void isLaunchingNextLoggedIn() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        LoginRepository.getInstance(new LoginDataSource())
                .setLoggedInUser(new LoggedInUser("0", "testUser"));
        Intent startedIntent = shadowOf(mainActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MenuActivity.class, shadowIntent.getIntentClass());
    }

    @Ignore("Problem with Android Keystore used in MainActivity")
    @After
    public void cleanUp() {
        LoginRepository.getInstance(new LoginDataSource())
                .logout();
    }

    /*
     * Link to the issue: https://github.com/robolectric/robolectric/issues/1518
     */

}