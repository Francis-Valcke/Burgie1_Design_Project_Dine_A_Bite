package com.example.attendeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity to show splash-screen on startup
 */
public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1000;

    /**
     * Called when app is first instantiated
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
            }
        }, SPLASH_TIME_OUT);
    }
}
