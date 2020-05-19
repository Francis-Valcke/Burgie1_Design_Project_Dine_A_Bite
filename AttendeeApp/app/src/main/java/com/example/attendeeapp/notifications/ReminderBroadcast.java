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
        int notificationID = intent.getIntExtra("notificationID", 1);
        int orderID = intent.getIntExtra("orderID", -1);
        intent.removeExtra("orderID");
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_DEPART_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Time to fetch order #" + orderID)
                .setContentText("Please depart now to your destination!")
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setColor(Color.RED);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationID, notification.build());
        notificationID++;
    }
}
