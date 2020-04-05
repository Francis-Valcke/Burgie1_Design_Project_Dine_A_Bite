package com.example.attendeeapp.polling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Class to help keep pollingService alive when app is not on top anymore
 */
public class RestartPolling extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");

        context.startService(new Intent(context, PollingService.class));

    }
}
