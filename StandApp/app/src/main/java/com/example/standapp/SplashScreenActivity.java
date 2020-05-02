package com.example.standapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.splash_screen);

        Thread loading = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                    Intent main = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(main);
                    finish();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    finish();
                }
            }
        };
        loading.start();
    }
}
