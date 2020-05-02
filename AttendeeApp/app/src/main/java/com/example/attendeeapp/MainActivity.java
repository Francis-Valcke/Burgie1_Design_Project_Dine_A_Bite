package com.example.attendeeapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.ui.login.LoginActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * MainActivity to show splash-screen on startup
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when app is first instantiated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);

        // Clear db of all entries (for testing purposes)
        //OrderDatabaseService orderDatabaseService = new OrderDatabaseService(getApplicationContext());
        //orderDatabaseService.deleteAllOrders();

        // Clear Shared Preference file (for testing purposes)
        // This will reset the logged in user -> you will again need to log in to the app
        /*
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.shared_pref_file_key),
                    masterKeyAlias,
                    this,
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
        */

        int SPLASH_TIME_OUT = 800;
        final Context mContext = this;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());

                // Fetch user credentials if stored
                LoggedInUser user;
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                            getString(R.string.shared_pref_file_key),
                            masterKeyAlias,
                            mContext,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                    String username = sharedPreferences.getString("username", null);
                    String userId = sharedPreferences.getString("user_id", null);
                    System.out.println("Username: " + username);
                    if (username != null && userId != null) {
                        user = new LoggedInUser(userId, username);
                        loginRepository.setLoggedInUser(user);
                    }
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }

                if (!loginRepository.isLoggedIn()) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(menuIntent);
                }
                finish();
            }

        }, SPLASH_TIME_OUT);

    }
}
