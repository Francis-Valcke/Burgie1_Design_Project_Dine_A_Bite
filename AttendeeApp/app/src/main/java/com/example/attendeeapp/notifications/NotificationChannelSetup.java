package com.example.attendeeapp.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * This class will setup up the channels that will be used for notifications.
 * The notification settings here are just the default settings,
 * but the user has ultimate control over these settings and can disable them whenever he wants
 */

public class NotificationChannelSetup extends Application {

    public static final String CHANNEL_START_ID = "orderStart";
    public static final String CHANNEL_DONE_ID = "orderDone";
    public static final String CHANNEL_DEPART_ID = "orderDepart";

    @Override
    public void onCreate() {
        super.onCreate();

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

            NotificationChannel orderDepart = new NotificationChannel(
                    CHANNEL_DEPART_ID,
                    "Order Depart",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderStart.setDescription("This will notify you when you need to depart to fetch your order in time");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(orderStart);
            manager.createNotificationChannel(orderDone);
        }
    }
}
