package com.example.attendeeapp.polling;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.MapsActivity;
import com.example.attendeeapp.OrderActivity;
import com.example.attendeeapp.R;
import com.example.attendeeapp.ServerConfig;
import com.example.attendeeapp.ToolbarActivity;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderStatusUpdate;
import com.example.attendeeapp.notifications.ReminderBroadcast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.example.attendeeapp.notifications.NotificationChannelSetup.CHANNEL_DEPART_ID;
import static com.example.attendeeapp.notifications.NotificationChannelSetup.CHANNEL_DONE_ID;
import static com.example.attendeeapp.notifications.NotificationChannelSetup.CHANNEL_START_ID;

/**
 * Service that polls the server for order updates
 */
public class PollingService extends Service {

    private Handler handler;
    private Context context;
    private int subscribeId;
    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();
    private NotificationManagerCompat notificationManager;
    private Location lastLocation = new Location("");
    private int notificationID = 0;

    private Map<Integer, LatLng> mapIdLocation = new HashMap<>();
    private Map<Integer, Integer> mapIdRemainingTime = new HashMap<>();

    public static final long DEFAULT_SYNC_INTERVAL = 5 * 1000;

    // Runnable that contains the order polling method
    private Runnable runnableService = new Runnable() {

        @Override
        public void run() {

            // Send the order and chosen stand ID to the server and confirm the chosen stand
            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscribeId;
            //checkLocationPermission();

            // Request a string response from the provided URL
            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {

                        ObjectMapper mapper = new ObjectMapper();
                        BetterResponseModel<List<Event>> responseModel = null;
                        try {
                            responseModel = mapper
                                    .readValue(response.toString(), new TypeReference<BetterResponseModel<List<Event>>>() {
                                    });
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "error while parsing event response", Toast.LENGTH_LONG).show();
                            return;
                        }


                        if (responseModel.isOk()) {


                            for (Event event : responseModel.getPayload()) {
                                JsonNode eventData = event.getEventData();
                                String eventClass = event.getDataType();

                                Intent intent = new Intent("orderUpdate");
                                switch (eventClass) {
                                    case "Order":
                                        try{
                                            JsonNode orderJson = eventData.get(eventClass.toLowerCase());
                                            CommonOrder order = mapper.readValue(orderJson.toString(), CommonOrder.class);
                                            intent.putExtra("orderUpdate", order);
                                        }
                                        catch (JsonProcessingException e){
                                            Toast.makeText(context, "error while parsing order", Toast.LENGTH_LONG).show();
                                        }
                                        break;

                                    case "OrderStatusUpdate":
                                        CommonOrderStatusUpdate orderStatusUpdate = null;
                                        try {
                                            orderStatusUpdate = mapper.readValue(eventData.toString(), CommonOrderStatusUpdate.class);
                                        } catch (JsonProcessingException e) {
                                            Toast.makeText(context, "error while parsing order update", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        intent.putExtra("orderStatusUpdate", orderStatusUpdate);

                                        // Initiate notification
                                        notificationManager = NotificationManagerCompat.from(context);
                                        // Create an Intent for the activity you want to start
                                        Intent activityIntent = new Intent(getApplication(), OrderActivity.class);
                                        // Create the TaskStackBuilder and add the intent, which inflates the back stack
                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
                                        stackBuilder.addNextIntentWithParentStack(activityIntent);
                                        // Get the PendingIntent containing the entire back stack
                                        PendingIntent resultPendingIntent =
                                                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground);

                                        if (orderStatusUpdate.getNewState() == CommonOrderStatusUpdate.State.CONFIRMED) {
                                            // Send Notification that order is being prepared

                                            // Get location of the attendee and location of the stand from which the attendee ordered
                                            LatLng stand_location = mapIdLocation.get(orderStatusUpdate.getOrderId());
                                            double stand_lat = stand_location.latitude;
                                            double stand_lon = stand_location.longitude;
                                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                            // Get remaining time of the order
                                            int remainingTimeOrder = mapIdRemainingTime.get(orderStatusUpdate.getOrderId()); // time in seconds

                                            String notificationText = "";
                                            if (lastLocation != null) {
                                                double my_lat = lastLocation.getLatitude();
                                                double my_lon = lastLocation.getLongitude();
                                                float[] result = new float[1];
                                                // Compute distance between both locations
                                                Location.distanceBetween(my_lat, my_lon, stand_lat, stand_lon, result); // distance in meters stored in result[0]
                                                System.out.println("STAND LOCATION: latitude: " + stand_lat + "; longitude: " + stand_lon);
                                                System.out.println("MY LOCATION: latitude: " + my_lat + "; longitude: " + my_lon);
                                                System.out.println("DISTANCE between both locations in meter: " + result[0] + " m and in km: " + result[0]/1000 + " km");
                                                double timeToDestination = result[0]/(5/3.6); // The time (in s) the attendee will need to walk to his destination
                                                double remainingTimeDepart = remainingTimeOrder - result[0]/(5/3.6);
                                                notificationText = "Your order will be ready in approximately " + Math.round(remainingTimeOrder/60.0) + " minute(s).\n" +
                                                        "Distance to your order: " + result[0] + " m\n" +
                                                        "Approximate time needed to walk to your destination: " + Math.round(timeToDestination/60.0) + " minute(s).";

                                                if (remainingTimeDepart <= 0) {
                                                    notificationText += "\nPlease depart now to your destination to fetch your order on time.";
                                                }
                                                else {
                                                    Intent intentReminderBroadcast = new Intent(PollingService.this, ReminderBroadcast.class);
                                                    intentReminderBroadcast.putExtra("notificationID", notificationID);
                                                    intentReminderBroadcast.putExtra("orderID", orderStatusUpdate.getOrderId());
                                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(PollingService.this, 0, intentReminderBroadcast, PendingIntent.FLAG_UPDATE_CURRENT);

                                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                                    long currentTime = System.currentTimeMillis();
                                                    long remainingTimeDepartMillis = Math.round(remainingTimeDepart * 1000);
                                                    alarmManager.set(AlarmManager.RTC_WAKEUP,
                                                            currentTime + remainingTimeDepartMillis,
                                                            pendingIntent);
                                                    notificationID++;
                                                    long periodDepart = Math.round(remainingTimeDepartMillis/(1000.0*60));
                                                    notificationText += "\nYou will be reminded within " + Math.round(remainingTimeDepartMillis/(1000.0*60)) + " minute(s) to leave for your order on time.";
                                                }
                                            }
                                            // in case the location of the attendee is null:
                                            else {
                                                System.out.println("Lastlocation is NULL!");
                                                notificationText += "Your order will be ready in approximately " + Math.round(remainingTimeOrder/60.0) + " minute(s).";
                                            }

                                            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_START_ID)
                                                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                                    .setContentTitle("Order #" + orderStatusUpdate.getOrderId() + " Confirmed")
                                                    .setContentText("Your order is being prepared!")
                                                    .setLargeIcon(largeIcon)
                                                    .setStyle(new NotificationCompat.BigTextStyle()
                                                            .bigText(notificationText)
                                                            .setBigContentTitle("Order #" + orderStatusUpdate.getOrderId() + " Confirmed")
                                                            .setSummaryText("Order details"))
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                                                    .setColor(Color.BLUE)
                                                    // Set the intent that will fire when the user taps the notification
                                                    .setContentIntent(resultPendingIntent)
                                                    .setAutoCancel(true);
                                            // notificationId is a unique int for each notification that you must define
                                            notificationManager.notify(notificationID, notification.build());
                                            notificationID++;

                                        } else if (orderStatusUpdate.getNewState() == CommonOrderStatusUpdate.State.READY) {
                                            // Send Notification that order is ready

                                            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_DONE_ID)
                                                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                                    .setContentTitle("Order #" + orderStatusUpdate.getOrderId() + " Ready")
                                                    .setContentText("Your order is ready to be picked up!")
                                                    .setLargeIcon(largeIcon)
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                                                    .setColor(Color.GREEN)
                                                    // Set the intent that will fire when the user taps the notification
                                                    .setContentIntent(resultPendingIntent)
                                                    .setAutoCancel(true);
                                            // notificationId is a unique int for each notification that you must define
                                            notificationManager.notify(notificationID, notification.build());
                                            notificationID++;
                                        }
                                        break;
                                }

                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        }
                    }, error -> {
                Toast mToast = null;
                mToast = Toast.makeText(context, "Polling failed",
                        Toast.LENGTH_SHORT);
                mToast.show();
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
            queue.add(jsonObject);

            handler.postDelayed(runnableService, DEFAULT_SYNC_INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();

        mapIdLocation = (HashMap<Integer, LatLng>) intent.getSerializableExtra("locations");
        mapIdRemainingTime = (HashMap<Integer, Integer>) intent.getSerializableExtra("remainingTime");
        lastLocation.setLatitude(intent.getDoubleExtra("myLocationLat", 0));
        lastLocation.setLongitude(intent.getDoubleExtra("myLocationLon", 0));
        assert lastLocation.getLatitude() != 0.0;

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
    public void onDestroy() {
        handler.removeCallbacks(runnableService);
        stopSelf();
        // Restart service when app is not on top, but still running (currently not working)
        /*Intent broadcastIntent = new Intent("restartpolling");
        broadcastIntent.setClass(this, RestartPolling.class);
        sendBroadcast(broadcastIntent);*/
        super.onDestroy();
    }
}

