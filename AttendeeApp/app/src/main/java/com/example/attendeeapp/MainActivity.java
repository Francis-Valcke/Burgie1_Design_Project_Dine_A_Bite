package com.example.attendeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendeeapp.order.CommonOrder;
import com.example.attendeeapp.roomDB.OrderDatabaseService;

/**
 * MainActivity to show splash-screen on startup
 */
public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 800;

    /**
     * Called when app is first instantiated
     * @param savedInstanceState
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
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
