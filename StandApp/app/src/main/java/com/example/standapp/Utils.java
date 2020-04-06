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

class Utils {

    private static boolean isConnected = false;

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
                    Toast.makeText(context, "Server cannot be reached, try again later", Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, "Please log in", Toast.LENGTH_LONG).show();
        } else {
            isLoggedIn = true;
        }

        return isLoggedIn;
    }
}
