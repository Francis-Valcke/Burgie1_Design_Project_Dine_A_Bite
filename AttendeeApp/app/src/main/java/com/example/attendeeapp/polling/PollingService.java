package com.example.attendeeapp.polling;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.MainActivity;
import com.example.attendeeapp.OrderActivity;
import com.example.attendeeapp.R;
import com.example.attendeeapp.ServerConfig;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderStatusUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Service that polls the server for order updates
 */
public class PollingService extends Service {

    private Handler handler;
    private Context context;
    private int subscribeId;
    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    public static final long DEFAULT_SYNC_INTERVAL = 5 * 1000;

    // Runnable that contains the order polling method
    private Runnable runnableService = new Runnable() {
        private int notificationID = 0;

        @Override
        public void run() {

            // Send the order and chosen stand ID to the server and confirm the chosen stand
            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = ServerConfig.EC_ADDRESS + "/events?id="+subscribeId;

            // Request a string response from the provided URL
            JsonArrayRequest jsonArray = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Toast mToast = null;
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(context, "Polling success",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                    Log.d("PollingService", "Service still running");

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    NotificationManager notificationManagerNew = null;
                    String channelID = "orderUpdates";
                    // Create the NotificationChannel, but only on API 26+ because
                    // the NotificationChannel class is new and not in the support library
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "orderUpdates";
                        String description = "Order progress updates";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel(channelID, name, importance);
                        channel.setDescription(description);
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this
                        notificationManagerNew = getSystemService(NotificationManager.class);
                        notificationManagerNew.createNotificationChannel(channel);
                    }*/

                    // Create an explicit intent for an Activity in your app
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(getApplication(), OrderActivity.class);
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            // Set the intent that will fire when the user taps the notification
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true);
                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(notificationID, builder.build());
                    notificationID++;


                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject event = (JSONObject) response.get(i);
                            JSONObject eventData = (JSONObject) event.get("eventData");
                            String eventClass = event.getString("dataType");

                            Intent intent = new Intent("orderUpdate");
                            switch(eventClass) {
                                case "Order":
                                    JSONObject orderJson = eventData.getJSONObject(eventClass.toLowerCase());
                                    CommonOrder order = mapper.readValue(orderJson.toString(), CommonOrder.class);
                                    intent.putExtra("orderUpdate", order);
                                    break;

                                case "OrderStatusUpdate":
                                    CommonOrderStatusUpdate orderStatusUpdate = mapper.readValue(eventData.toString(), CommonOrderStatusUpdate.class);
                                    intent.putExtra("orderStatusUpdate", orderStatusUpdate);
                                    break;
                            }

                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException | JsonProcessingException e) {
                        Log.v("JSONException", "JSONException in polling service");
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast mToast = null;
                    mToast = Toast.makeText(context, "Polling failed",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }) {
                // Add JSON headers
                @Override
                public @NonNull
                Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", user.getAuthorizationToken());
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            queue.add(jsonArray);

            handler.postDelayed(runnableService, DEFAULT_SYNC_INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        handler = new Handler();
        handler.post(runnableService);
        Log.i("Polling service", "Polling service started");
        subscribeId = intent.getIntExtra("subscribeId", -1);

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        if (handler != null) handler.removeCallbacks(runnableService);
        stopSelf(); // MUST BE CALLED to stop restart after START_REDELIVER_INTENT was used
        // (if service must not be restarted)

        /*Intent broadcastIntent = new Intent("restartpolling");
        broadcastIntent.putExtra("subscribeId", subscribeId);
        broadcastIntent.setClass(this, RestartPolling.class);
        sendBroadcast(broadcastIntent);*/
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacks(runnableService);
        stopSelf();
        // Restart service when app is not on top, but still running (currently not working)
        /*Intent broadcastIntent = new Intent("restartpolling");
        broadcastIntent.setClass(this, RestartPolling.class);
        sendBroadcast(broadcastIntent);*/
        super.onDestroy();
    }

}
