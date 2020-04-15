package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.view.MenuItem;

import android.os.Bundle;

import com.example.standapp.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ProfileFragment profile;
    private OrderFragment order;
    private DashboardFragment dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Start logging in activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);

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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profile).commit();
                break;
            case R.id.nav_orders:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, order).commit();
                break;
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dashboard).commit();
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

        // Start profile fragment after successfully logging in
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profile).commit();
    }
}
