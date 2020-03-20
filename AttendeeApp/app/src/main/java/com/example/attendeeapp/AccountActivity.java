package com.example.attendeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;

// TODO log out button and functionality
// TODO show history of picked up orders

public class AccountActivity extends AppCompatActivity {

    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle("Account");

        // Show username of currently logged in user
        LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();
        username = findViewById(R.id.username);
        username.setText(user.getDisplayName());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon
                // on the main android toolbar.
                onBackPressed();
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                Intent intent = new Intent(AccountActivity.this, OrderActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_action:
                // User chooses the "Account" item
                Intent intent2 = new Intent(AccountActivity.this, AccountActivity.class);
                startActivity(intent2);
                return true;
            case R.id.settings_action:
                // User chooses the "Settings" item
                // TODO make settings activity
                return true;
            case R.id.map_action:
                //User chooses the "Map" item
                Intent mapIntent = new Intent(AccountActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
