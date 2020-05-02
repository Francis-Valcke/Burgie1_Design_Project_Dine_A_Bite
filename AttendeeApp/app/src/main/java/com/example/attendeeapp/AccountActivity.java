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

public class AccountActivity extends ToolbarActivity {

    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

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
        if (item.getItemId() == R.id.account_action) {
            // User chooses the "Account" item
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
