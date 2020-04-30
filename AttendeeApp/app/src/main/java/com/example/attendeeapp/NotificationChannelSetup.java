package com.example.attendeeapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannelSetup extends Application {
    public static final String CHANNEL_START_ID = "orderStart";
    public static final String CHANNEL_DONE_ID = "orderDone";


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    // The notification settings here are just the default settings, but the user has ultimate control over these settings and can disable them whenever he wants
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel orderStart = new NotificationChannel(
                    CHANNEL_START_ID,
                    "Order Start",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderStart.setDescription("This will notify you when your order is being prepared");

            NotificationChannel orderDone = new NotificationChannel(
                    CHANNEL_DONE_ID,
                    "Order Done",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderDone.setDescription("This will notify you when your order is ready to be picked up");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(orderStart);
            manager.createNotificationChannel(orderDone);
        }
    }
}