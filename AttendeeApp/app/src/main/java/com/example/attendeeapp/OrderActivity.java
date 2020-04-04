package com.example.attendeeapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.order.CommonOrder;
import com.example.attendeeapp.polling.PollingService;
import com.example.attendeeapp.appDatabase.OrderDatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity to handle the show order overview
 */
public class OrderActivity extends AppCompatActivity {

    private int subscribeId;

    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CommonOrder orderUpdate = (CommonOrder) intent.getSerializableExtra("orderUpdate");
        }
    };

    private boolean isPollingServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(PollingService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);


        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        final CommonOrder newOrder = (CommonOrder) getIntent().getSerializableExtra("order");
        int stand = getIntent().getIntExtra("standID", 0);

        // Database initialization
        OrderDatabaseService orderDatabaseService = new OrderDatabaseService(getApplicationContext());
        ArrayList<CommonOrder> orders = (ArrayList<CommonOrder>) orderDatabaseService.getAll();

        if(orders.size() == 0 && newOrder == null) {
            // No (new) orders
            return;

        } else if (newOrder == null) {
            // Initiate the expandable order ListView
            ExpandableListView expandList = (ExpandableListView)findViewById(R.id.order_expand_list);
            OrderItemExpandableAdapter adapter = new OrderItemExpandableAdapter(this, orders);

            expandList.setAdapter(adapter);
            return;
        }

        orders.add(newOrder);
        orderDatabaseService.insertOrder(newOrder);

        // Initiate the expandable order ListView
        ExpandableListView expandList = (ExpandableListView)findViewById(R.id.order_expand_list);
        OrderItemExpandableAdapter adapter = new OrderItemExpandableAdapter(this, orders);

        expandList.setAdapter(adapter);

        // Register as subscriber to the orderId event channel
        if (!isPollingServiceRunning(PollingService.class)){
            getSubscriberId(newOrder.getId());
        }

        // Send the order and chosen stand ID to the server and confirm the chosen stand
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String om_url = String.format("http://cobol.idlab.ugent.be:8091/confirmStand?order_id=%1$s&stand_id=%2$s", newOrder.getId(), stand);
        String om_url = ServerConfig.OM_ADDRESS + "/pingOM";

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, om_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Your order was successful",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Your final order could not be received",
                        Toast.LENGTH_SHORT);
                mToast.show();

            }
        }) {
            // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders()  throws AuthFailureError {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the listener for polling updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("orderUpdate"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    /**
     * Subscribes to the server eventChannel for the given order
     * and start polling for events in a polling service
     */
    public void getSubscriberId(int orderId) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://cobol.idlab.ugent.be:8093/registerSubscriber?types=o"+orderId;

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                subscribeId = Integer.parseInt(response);
                // Start the polling service
                Intent intent = new Intent(getApplicationContext(), PollingService.class);
                intent.putExtra("subscribeId", subscribeId);
                startService(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Could not subscribe to order updates",
                        Toast.LENGTH_SHORT);
                mToast.show();

            }
        }) {
            // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders()  throws AuthFailureError {
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
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon
                // on the main android toolbar.
                onBackPressed();
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                return true;
            case R.id.account_action:
                // User chooses the "Account" item
                // TODO make account activity
                return true;
            case R.id.settings_action:
                // User chooses the "Settings" item
                // TODO make settings activity
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
