package com.example.attendeeapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

/**
 * Test for the main activity
 */
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    private MainActivity mActivity = null;
    Instrumentation.ActivityMonitor monitor = InstrumentationRegistry.getInstrumentation()
            .addMonitor(MenuActivity.class.getName(), null, false);

    @Before
    public void setUp() throws Exception {
        mActivity = rule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
        mActivity = null;
    }

    /**
     * Tests if main activity launch is successful
     */
    @Test
    public void testLaunch() {
        View view = mActivity.findViewById(R.id.activity_main);
        assertNotNull(view);
        assertTrue(view instanceof LinearLayout);
    }

    /**
     * Tests if mainActivity launches menu activity
     */
    @Test
    public void isLaunchingNext() {
        Intents.init();
        try {
            TimeUnit.MILLISECONDS.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        intended(hasComponent(MenuActivity.class.getName()));
        Intents.release();
    }
}