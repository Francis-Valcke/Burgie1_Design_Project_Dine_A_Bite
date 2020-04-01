package com.example.attendeeapp.polling;

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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.order.CommonOrder;
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

    public static final long DEFAULT_SYNC_INTERVAL = 10 * 1000;

    private int subscribeId;
    // Runnable that contains the order polling method
    private Runnable runnableService = new Runnable() {
        @Override
        public void run() {

            // Send the order and chosen stand ID to the server and confirm the chosen stand
            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://cobol.idlab.ugent.be:8093/events?id="+79;

            // Request a string response from the provided URL
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    String details = null;
                    try {
                        details = (String) response.get("details");
                        JSONArray detailsJSON= new JSONArray(details);
                        for(int i =0 ; i<detailsJSON.length(); i++){
                            JSONObject event = (JSONObject) detailsJSON.get(0);
                            JSONObject eventData = (JSONObject) event.get("eventData");
                            JSONObject orderJson = eventData.getJSONObject("order");
                            CommonOrder order = mapper.readValue(orderJson.toString(), CommonOrder.class);


                            Intent intent = new Intent("orderUpdate");
                            intent.putExtra("orderUpdate", order);
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
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(context, "Polling failed",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }) {
                // Add JSON headers
                @Override
                public @NonNull
                Map<String, String> getHeaders()  throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi" +
                            "JmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYX" +
                            "QiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0O" +
                            "UlsbAPNyS9_vr-1QcOWnQ");
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            queue.add(stringRequest);

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
    public void onDestroy() {
        handler.removeCallbacks(runnableService);
        stopSelf();
        // Restart service when app is not on top, but still running
        Intent broadcastIntent = new Intent("restartpolling");
        broadcastIntent.setClass(this, RestartPolling.class);
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

}
