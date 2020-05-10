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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.ServerConfig;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.order.Event;
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
            JsonArrayRequest jsonArray = new JsonArrayRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Toast mToast = Toast.makeText(context, "Polling success",
                            Toast.LENGTH_SHORT);
                    mToast.show();

                    System.out.println("RESPONSE: " + response.toString());

                    ObjectMapper mapper = new ObjectMapper();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject event = (JSONObject) response.get(i);
                            Event eventUpdate = mapper.readValue(event.toString(), Event.class);

                            Intent intent = new Intent("eventUpdate");
                            intent.putExtra("eventUpdate", eventUpdate);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        } catch (JSONException | JsonProcessingException e) {
                            e.printStackTrace();
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
            queue.add(jsonArray);

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
