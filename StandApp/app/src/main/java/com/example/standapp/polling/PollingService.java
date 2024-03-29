package com.example.standapp.polling;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.ServerConfig;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.BetterResponseModel;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service that polls the server for order updates
 */
public class PollingService extends Service {

    private Context context;
    private Handler handler;

    public static final long DEFAULT_SYNC_INTERVAL = 10 * 1000;

    private final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    // Subscriber ID for Event Channel
    private int subscribeId;

    // Runnable that contains the order polling method
    private Runnable runnableService = new Runnable() {
        @Override
        public void run() {

            // Send the order and chosen stand ID to the server and confirm the chosen stand
            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscribeId;

            // Request a string response from the provided URL
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    BetterResponseModel<List<Event>> responseModel = null;
                    try {
                        responseModel = mapper.readValue(response.toString(),
                                new TypeReference<BetterResponseModel<List<Event>>>() {});

                    } catch (JsonProcessingException e) {
                        Toast.makeText(context, "Error while parsing response for orders",
                                Toast.LENGTH_LONG).show();
                    }

                    if (responseModel!=null) {
                       if (responseModel.isOk()) {
                           for (Event event : responseModel.getPayload()) {
                               System.out.println("New event: " + event.toString());
                               Intent intent = new Intent("eventUpdate");
                               intent.putExtra("eventUpdate", event);
                               LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                           }
                       }
                       else {
                           Toast.makeText(context, responseModel.getDetails(),
                                   Toast.LENGTH_LONG).show();
                       }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast mToast = Toast.makeText(context, "Polling failed",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }) {
                @Override
                public @NonNull
                Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", user.getAuthorizationToken());
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            queue.add(jsonObjectRequest);

            handler.postDelayed(runnableService, DEFAULT_SYNC_INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        handler = new Handler();

        boolean ret = handler.post(runnableService);
        System.out.println("Was the runnableService successfully launched: " + ret);
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
        System.out.println("ON_DESTROY CALLED IN POLLING_SERVICE CLASS");
        handler.removeCallbacks(runnableService);
        stopSelf();
        super.onDestroy();
    }

}
