package com.example.attendeeapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.attendeeapp.R;

import static com.example.attendeeapp.notifications.NotificationChannelSetup.CHANNEL_DEPART_ID;

public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationID = 1000; //intent.getIntExtra("notificationID", 1);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_DEPART_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Time to fetch order")
                .setContentText("Depart now to your destination to fetch your order in time!")
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.BLUE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationID, notification.build());
        notificationID++;
    }
}
