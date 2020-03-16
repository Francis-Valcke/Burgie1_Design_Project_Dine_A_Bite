package com.example.standapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(android.R.color.holo_green_light)
                .withHeaderText("Welcome")
                .withFooterText("Copyright 2020")
                .withBeforeLogoText("Stand Application")
                .withAfterLogoText("Making sure your event runs smoothly")
                .withLogo(R.mipmap.ic_launcher_round);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);
    }
}
