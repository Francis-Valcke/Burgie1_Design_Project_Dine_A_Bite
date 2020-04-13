package com.example.standapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.standapp.json.CommonFood;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        //TODO: unable to performClick on the dialog (because MaterialAlertDialogBuilder has no button which I can find with FindViewById and click on)
        //dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
    }

    /**
     * Tests if adding, deleting and editing menu items works
     */
    @Test
    public void addDeleteEditItemsDashboard() {
        DashboardListViewAdapter adapter = dashboardFragment.getAdapter();

        // Test adding items

        BigDecimal price = new BigDecimal(5);
        List<String> category = new ArrayList<>();
        CommonFood item = new CommonFood("Burger", price, 150, 20, "", "", category);
        dashboardFragment.addFoodToMenu(item);
        assertEquals(1, dashboardFragment.getItems().size());
        assertEquals(price.intValue(), dashboardFragment.getItems().get(0).getPrice().intValue());
        assertEquals("Burger", dashboardFragment.getItems().get(0).getName());
        assertEquals(150, dashboardFragment.getItems().get(0).getPreparationTime());
        assertEquals(20, dashboardFragment.getItems().get(0).getStock());

        for (int i = 0; i < 15; i++) {
            CommonFood item_next = new CommonFood("Burger"+i, price, 150, 20, "", "", category);
            dashboardFragment.addFoodToMenu(item_next);
        }
        assertEquals(1 + 15, dashboardFragment.getItems().size());

        // Test deleting items

        for (int i = 0; i < 5; i++) {
            adapter.removeItem(0);
        }
        assertEquals(16 - 5, dashboardFragment.getItems().size());

        // Test editing items

        BigDecimal price_edit = new BigDecimal(5000);
        adapter.editItem(0, "BURGER EDIT", price_edit, 22075);
        assertEquals("BURGER EDIT", dashboardFragment.getItems().get(0).getName());
        assertEquals(price_edit.intValue(), dashboardFragment.getItems().get(0).getPrice().intValue());
        assertEquals(22075, dashboardFragment.getItems().get(0).getStock());
    }

    private void startFragment( Fragment fragment ) {
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null );
        fragmentTransaction.commit();
    }
}