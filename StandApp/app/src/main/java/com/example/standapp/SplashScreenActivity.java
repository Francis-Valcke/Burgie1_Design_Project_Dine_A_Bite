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

        /*EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(2500)
                .withLogo(R.mipmap.ic_launcher_foreground);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);*/
    }
}
