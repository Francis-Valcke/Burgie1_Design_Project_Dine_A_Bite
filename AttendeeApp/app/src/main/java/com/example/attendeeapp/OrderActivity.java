package com.example.attendeeapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderStatusUpdate;
import com.example.attendeeapp.polling.PollingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to handle the show order overview
 * Loads previous orders from the internal database stored on the divide
 */
public class OrderActivity extends ToolbarActivity {

    private int subscribeId = -1;
    private Switch runningOrderSwitch;
    private ArrayList<CommonOrder> orders;
    private OrderDatabaseService orderDatabaseService;
    private OrderItemExpandableAdapter adapter;
    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CommonOrder orderUpdate = (CommonOrder) intent.getSerializableExtra("orderUpdate");
            CommonOrderStatusUpdate orderStatusUpdate = (CommonOrderStatusUpdate) intent.getSerializableExtra("orderStatusUpdate");
            if (orderUpdate != null) {
                // Update all order fields
                //adapter.notifyDataSetChanged();
            }
            if (orderStatusUpdate != null) {
                // Update order status fields
                adapter.updateOrder(orderStatusUpdate.getOrderId(), orderStatusUpdate.getNewStatus());
                adapter.notifyDataSetChanged();
            }
        }
    };

    // If the service polling for order updates is running or not
    private boolean isPollingServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        runningOrderSwitch= findViewById(R.id.running_order_switch);
        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

        final CommonOrder newOrder = (CommonOrder) getIntent().getSerializableExtra("order");

        orders= new ArrayList<>();

        // Database initialization and loading of the stored data
        orderDatabaseService = new OrderDatabaseService(getApplicationContext());
        adapter = new OrderItemExpandableAdapter(this, orders, orderDatabaseService);
        // Initiate the expandable order ListView
        ExpandableListView expandList = findViewById(R.id.order_expand_list);
        expandList.setAdapter(adapter);
        updateUserOrdersFromDB();

        // add event listener to switch
        runningOrderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               updateUserOrdersFromDB();
            }
        });

        if (orders == null || (orders.size() == 0 && newOrder == null)) {
            // No (new) orders
            return;

        } else if (newOrder != null) {
            // Send the order and chosen stand and brandName to the server and confirm the chosen stand
            String chosenStand = getIntent().getStringExtra("stand");
            String chosenBrand = getIntent().getStringExtra("brand");
            newOrder.setStandName(chosenStand);
            newOrder.setBrandName(chosenBrand);
            confirmNewOrderStand(newOrder, chosenStand, chosenBrand);
        }




        // Register as subscriber to the orderId event channel
        if (!isPollingServiceRunning(PollingService.class)){
            ArrayList<Integer> orderIds = new ArrayList<>();
            for (CommonOrder order : orders) {
                orderIds.add(order.getId());
            }
            if (newOrder != null) orderIds.add(newOrder.getId());
            // orderId's will not be empty, else this code is not reachable
            getSubscriberId(orderIds);
        }





    }

    @Override
    public void onResume() {
        super.onResume();
        // Start polling service
        if (subscribeId != -1) {
            Intent intent = new Intent(getApplicationContext(), PollingService.class);
            intent.putExtra("subscribeId", subscribeId);
            startService(intent);
        }
        // Register the listener for polling updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("orderUpdate"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the polling service
        stopService(new Intent(getApplicationContext(), PollingService.class));

        // Unregister the listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }


    /**
     * Confirm the chosen stand and brand when a new order is made
     * @param newOrder
     * @param chosenStand
     * @param chosenBrand
     */
    public void confirmNewOrderStand(final CommonOrder newOrder, String chosenStand, String chosenBrand) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS;
        url = String.format("%1$s/confirmStand?orderId=%2$s&standName=%3$s&brandName=%4$s",
                url,
                newOrder.getId(),
                chosenStand.replace("&","%26"),
                chosenBrand.replace("&","%26"));
        url = url.replace(' ' , '+');

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
            public @NonNull Map<String, String> getHeaders()  {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }


    public void updateUserOrdersFromDB(){


        RequestQueue queue= Volley.newRequestQueue(this);
        String url= ServerConfig.OM_ADDRESS;
        url=String.format("%1$s/getUserOrders", url);

        JsonArrayRequest request= new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                ObjectMapper mapper= new ObjectMapper();
                try {
                    List<CommonOrder> allUserOrders= mapper.readValue(response.toString(),
                            new TypeReference<List<CommonOrder>>() {});

                    // TODO: implement something better to update local database @Nathan
                    /*orderDatabaseService.deleteAllOrders();
                    for (CommonOrder order : allUserOrders) {
                        orderDatabaseService.insertOrder(order);
                    }*/

                    orders.clear();
                    orders.addAll(allUserOrders);

                    ArrayList<CommonOrder> readyOrders= new ArrayList<CommonOrder>();
                    if(runningOrderSwitch.isChecked()){
                        for (CommonOrder order : orders) {
                            if(order.getOrderState()== CommonOrder.State.READY){
                                readyOrders.add(order);
                            }
                        }
                        orders.removeAll(readyOrders);
                    }



                    Collections.sort(orders, new Comparator<CommonOrder>() {
                        @Override
                        public int compare(CommonOrder o1, CommonOrder o2) {
                            return o1.getId()- o2.getId();
                        }
                    });
                    adapter.notifyDataSetChanged();


                    Toast mToast = null;
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(OrderActivity.this, "Orders updated",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Your orders could not be retrieved from the server",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        }){
            // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders()  {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(request);
    }



    /**
     * Subscribes to the server eventChannel for the given order
     * and launch the polling service to poll for events (order updates) from the server
     * @param orderId : list of order id's that must be subscribed to
     *                TODO: unregister subscriber
     *                      save subscriberId instead of asking new one every time
     */
    public void getSubscriberId(ArrayList<Integer> orderId) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber?types=o_" + orderId.get(0);
        for (Integer i : orderId.subList(1, orderId.size())) {
            url = url.concat(",o_" + i);
        }

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
            public @NonNull Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
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
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.orders_action) {
            // User chooses the "My Orders" item
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
