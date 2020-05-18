package com.example.standapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.robolectric.Robolectric;


// https://developer.android.com/training/testing/unit-testing/local-unit-tests
// https://medium.com/@chirag.jain5000/unit-testing-in-android-is-a-mess-80694ebbde07
// https://www.philosophicalhacker.com/2015/04/17/why-android-unit-testing-is-so-hard-pt-1/
// https://github.com/robolectric/robolectric/issues/1518

@Ignore("Nothing that can be tested")
public class MainActivityTest {

    private MainActivity mainActivity;

    @Ignore("No setup needed")
    @Before
    public void setUp() {
        mainActivity = Robolectric.buildActivity( MainActivity.class )
                .create().start().get();
    }

    /**
     * Tests if mainActivity launch is successful
     *
     * https://github.com/robolectric/robolectric/issues/1518
     */
    @Ignore("Literally impossible to test an activity using Android Keystore")
    public void testLaunch() {
        Assert.assertNotNull(mainActivity);
    }

}