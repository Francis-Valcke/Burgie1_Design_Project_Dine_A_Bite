package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;

import android.os.Bundle;

import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ProfileFragment profile;
    private OrderFragment order;
    private DashboardFragment dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context mContext = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        profile = new ProfileFragment();
        order = new OrderFragment();
        dashboard = new DashboardFragment();

        // To pass data in between fragments
        Bundle bundle = new Bundle();
        profile.setArguments(bundle);
        dashboard.setArguments(bundle);
        order.setArguments(bundle);

        // Fetch user credentials if stored
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        fetchCredentials(mContext, bundle, loginRepository);

        if (!loginRepository.isLoggedIn()) {
            // Start logging in activity when not user credentials stored (not logged in)
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else {
            // Start profile fragment after successfully retrieving user credentials (is logged in)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profile)
                    .commit();
        }

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_profile);
        }

    }

    /**
     * This function is called when an item is clicked in the navigation drawer,
     * and depending on the item clicked, the corresponding switch case will be selected
     * @param item the item in navigation drawer that was selected by the user
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profile)
                        .commit();
                break;
            case R.id.nav_orders:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, order)
                        .commit();
                break;
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, dashboard)
                        .commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Start profile fragment after successfully logging in or registering
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profile)
                .commit();
    }

    /**
     * Fetch the credentials stored on the Android device
     * - username
     * - user ID (Authorization token)
     * - stand name
     * - brand name
     * - subscriber ID for the Event Channel
     *
     * @param context context from which this method is called
     * @param bundle bundle to store the retrieved credentials in
     * @param loginRepository stores the logged in user
     */
    private void fetchCredentials(Context context, Bundle bundle, LoginRepository loginRepository) {
        LoggedInUser user;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.shared_pref_file_key),
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            String username = sharedPreferences.getString("username", null);
            String userId = sharedPreferences.getString("user_id", null); // token
            String standName = sharedPreferences.getString("stand_name", null);
            String brandName = sharedPreferences.getString("brand_name", null);
            String subscriberId = sharedPreferences.getString("subscriber_id", null);
            System.out.println("Username: " + username); // DEBUG
            if (username != null && userId != null) {
                user = new LoggedInUser(userId, username);
                loginRepository.setLoggedInUser(user);
                if (standName != null && brandName != null && subscriberId != null) {
                    bundle.putString("standName", standName);
                    bundle.putString("brandName", brandName);
                    bundle.putString("subscriberId", subscriberId);
                    profile.fetchMenu(context, standName, brandName, bundle);
                    profile.fetchRevenue(context, standName, brandName);
                }
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear Shared Preference file, this will reset the logged in user
     * - erase username
     * - erase user ID / token
     * - erase subscriber ID
     *
     * @param context context from which the method is called
     */
    public void clearCredentials(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.shared_pref_file_key),
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            // Ignore warning: needs to be commit to be synchronous
            editor.commit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public MenuItem getItem() {
        return navigationView.getCheckedItem();
    }

    public void setItem(int ID) {
        navigationView.setCheckedItem(ID);
    }
}
