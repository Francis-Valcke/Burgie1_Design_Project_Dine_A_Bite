package com.example.attendeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.ui.login.LoginActivity;

/**
 * MainActivity to show splash-screen on startup
 */
public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 800;

    /**
     * Called when app is first instantiated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Clear db of all entries (for testing purposes)
        //OrderDatabaseService orderDatabaseService = new OrderDatabaseService(getApplicationContext());
        //orderDatabaseService.deleteOrder(new CommonOrder());

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
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
