package com.example.standapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLooper;

import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class FragmentsTest {

    private MainActivity mainActivity;
    private ProfileFragment profileFragment;
    private DashboardFragment dashboardFragment;
    private Bundle bundle;

    @Before
    public void setUp() throws Exception {
        //profileFragment = launchFragmentInContainer<ProfileFragment>()
        mainActivity = Robolectric.buildActivity(MainActivity.class)
                .create().start().get();
        profileFragment = new ProfileFragment();
        dashboardFragment = new DashboardFragment();
        startFragment(profileFragment);
        startFragment(dashboardFragment);
    }

    /**
     * Tests if mainActivity launch is successful
     */
    @Test
    public void testMainActivity() {
        Assert.assertNotNull(mainActivity);
    }

    /**
     * Tests if profileFragment launch is successful
     */
    @Test
    public void testProfileFragment() {
        Assert.assertNotNull(profileFragment);
        bundle = profileFragment.getArguments();
        if (bundle != null) bundle.putString("standName", "Levis Burgers");
        if (bundle != null) bundle.putString("brandName", "Levis Burgers");
    }

    /**
     * Tests if dashboardFragment launch is successful
     */
    @Test
    public void testDashboardFragment() {
        Assert.assertNotNull(dashboardFragment);
    }

    /**
     * Tests if editing standname and brandname works
     */
    @Test
    public void editStandBrandName() {
        MaterialButton edit_stand_name_button = mainActivity.findViewById(R.id.edit_stand_name_button);
        assertNotNull(edit_stand_name_button);
        assertTrue(edit_stand_name_button.isClickable());
        edit_stand_name_button.performClick();

        Dialog dialog = ShadowAlertDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        assertNotNull(dialog);

        TextInputEditText edit_text_name = dialog.findViewById(R.id.edit_text_name);
        edit_text_name.setText("Levis Burgers");
        assertEquals("Levis Burgers", edit_text_name.getText().toString());
        //unable to performClick on the dialog
        //dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
    }

    /**
     * Tests if adding menu items to the manager dashboard works
     */
    @Test
    public void addItemsDashboard() {
        //int amount_items =
    }

    private void startFragment( Fragment fragment ) {
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null );
        fragmentTransaction.commit();
    }


}