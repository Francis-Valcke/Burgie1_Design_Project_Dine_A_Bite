package com.example.standapp;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Utils {

    private static boolean isConnected = false;
    private static String subscriberId;

    /**
     * Show if internet connection and server connection are online
     * and show in a toast message to user
     * @param context : the context from where the test connection is called
     * @return boolean is connected or not
     */
    static boolean isConnected(final Context context) {

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);
        String om_url = ServerConfig.OM_ADDRESS + "/pingOM";

        // Request a string response (ping message) from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, om_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                isConnected = true;
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isConnected = false;
                System.out.println(error.toString());
                if (error instanceof NoConnectionError) {
                    Toast.makeText(context, "No network connection", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Server cannot be reached, try again later",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Add the request to the RequestQueue
        queue.add(stringRequest);

        return isConnected;
    }

    /**
     * Show if user is logged in to the system or not
     * @param context : from where the method is called
     * @param bundle : the bundle that contains the log in information
     * @return boolean user is logged in or not
     */
    static boolean isLoggedIn(Context context, Bundle bundle) {
        boolean isLoggedIn = false;
        String standName = bundle.getString("standName");
        String brandName = bundle.getString("brandName");

        if (standName == null || standName.isEmpty() || brandName == null || brandName.isEmpty()) {
            Toast.makeText(context, "Please log in to your stand", Toast.LENGTH_LONG).show();
        } else {
            isLoggedIn = true;
        }

        return isLoggedIn;
    }

    /**
     * Subscribe to the Event Channel
     * @param mContext context from where the method is called
     * @param brandName name of the brand of the stand
     * @param standName name of the stand
     * @return subscriber ID given bij Event Channel
     */
    private String subscribeEC(final Context mContext, final String standName, final String brandName) {

        // Step 1: Get subscriber ID
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(mContext));
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber";

        // GET request to server
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(mContext, "SubscriberId: " + response, Toast.LENGTH_SHORT)
                                .show();
                        subscriberId = response;

                        // Step 2: Subscribe to stand and subscriberID channels
                        System.out.println("SubscriberID = " + subscriberId);
                        String url2 = ServerConfig.EC_ADDRESS + "/registerSubscriber/toChannel?type=s_"
                                + standName
                                + "_" + brandName + "&id=" + subscriberId;
                        url2 = url2.replace(' ', '+');

                        // GET request to server
                        final String finalUrl = url2;
                        StringRequest request2 = new StringRequest(Request.Method.GET, url2,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        System.out.println("Response on GET request to " + finalUrl + ": " + response);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(mContext, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", ServerConfig.AUTHORIZATION_TOKEN);
                                return headers;
                            }
                        };
                        RequestQueue queue2 = Volley.newRequestQueue(Objects.requireNonNull(mContext));
                        queue2.add(request2);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(mContext, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", ServerConfig.AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        queue.add(request);
        return subscriberId;
    }
}
