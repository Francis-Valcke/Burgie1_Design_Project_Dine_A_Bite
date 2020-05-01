package com.example.attendeeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

        Button logOutButton = findViewById(R.id.button_log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Delete persistent data from the currently logged in user
                // (clear db of all entries)
                OrderDatabaseService orderDatabaseService =
                        new OrderDatabaseService(getApplicationContext());
                orderDatabaseService.deleteAllOrders();

                // Clear Shared Preference file, this will reset the logged in user
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                            getString(R.string.shared_pref_file_key),
                            masterKeyAlias,
                            getApplicationContext(),
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

                LoginRepository.getInstance(new LoginDataSource()).logout();

                // Start MainActivity again
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        });
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
