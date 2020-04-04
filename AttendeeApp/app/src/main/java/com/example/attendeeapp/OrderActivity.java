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
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.polling.PollingService;
import com.example.attendeeapp.appDatabase.OrderDatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;

/**
 * Activity to handle the show order overview
 * Loads previous orders from the internal database stored on the devide
 */
public class OrderActivity extends AppCompatActivity {

    private int subscribeId;
    private ArrayList<CommonOrder> orders;
    private OrderDatabaseService orderDatabaseService;
    private OrderItemExpandableAdapter adapter;

    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CommonOrder orderUpdate = (CommonOrder) intent.getSerializableExtra("orderUpdate");
        }
    };

    // If the service polling for order updates is running or not
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

        // Database initialization and loading of the stored data
        orderDatabaseService = new OrderDatabaseService(getApplicationContext());
        orders = (ArrayList<CommonOrder>) orderDatabaseService.getAll();

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

        // Initiate the expandable order ListView
        ExpandableListView expandList = (ExpandableListView)findViewById(R.id.order_expand_list);
        adapter = new OrderItemExpandableAdapter(this, orders);

        expandList.setAdapter(adapter);

        // Register as subscriber to the orderId event channel
        // to be used later
        /*if (!isPollingServiceRunning(PollingService.class)){
            getSubscriberId(newOrder.getId());
        }*/

        // Send the order and chosen stand and brandName to the server and confirm the chosen stand
        String chosenStand = getIntent().getStringExtra("stand");
        String chosenBrand = getIntent().getStringExtra("brand");
        newOrder.setStandName(chosenStand);
        newOrder.setBrandName(chosenBrand);
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS;
        url = String.format("%1$s/confirmStand?orderId=%2$s&standName=%3$s&brandName=%4$s",
                url,
                newOrder.getId(),
                chosenStand.replace("&","%26"),
                chosenBrand.replace("&","%26"));

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // Only add the order if successful
                orders.add(newOrder);
                orderDatabaseService.insertOrder(newOrder);
                adapter.notifyDataSetChanged();

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
                headers.put("Authorization", AUTHORIZATION_TOKEN);
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
     * and launch the polling service to poll for events (order updates) from the server
     */
    public void getSubscriberId(int orderId) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber?types=o" + orderId;

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
                headers.put("Authorization", AUTHORIZATION_TOKEN);
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
