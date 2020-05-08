package com.example.attendeeapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import java.util.ArrayList;
import java.util.Collections;
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
        // -- init UI -- //
        setContentView(R.layout.activity_order);
        initToolbar();
        upButtonToolbar();

        // order(s) passed by confirm order activity
        ArrayList<CommonOrder> newOrderList= (ArrayList<CommonOrder>) getIntent().getSerializableExtra("orderList");

        runningOrderSwitch = findViewById(R.id.running_order_switch);
        // add event listener to switch
        runningOrderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateUserOrdersFromDB());


        // -- data init -- //
        orderDatabaseService = new OrderDatabaseService(getApplicationContext());

        // initialize orders to present
        orders = new ArrayList<>();
        adapter = new OrderItemExpandableAdapter(this, orders, orderDatabaseService);

        // Couple data to UI
        ExpandableListView expandList = findViewById(R.id.order_expand_list);
        expandList.setAdapter(adapter);
        updateUserOrdersFromDB();


        // If there are no orders, nothing to poll
        if (orders == null || (orders.size() == 0 && newOrderList == null)) {
            // No (new) orders
            return;
        } else if (newOrderList != null) {
            // Send all orders of the list to the server and confirm the chosen stands
            for (CommonOrder commonOrder : newOrderList) {
                confirmNewOrderStand(commonOrder);
            }
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
     *  Check if polling service is running and if not request a new subscriberId for the current orders
     */
    private void subscribeToOrderUpdates() {
        // TODO: register to channel of new order non-confirmed orders are no longer present
        //  (pollingservice is running already when new order confirmation is received)
        // Register as subscriber to the orderId event channel
        if (!isPollingServiceRunning(PollingService.class)) {
            ArrayList<Integer> orderIds = new ArrayList<>();
            for (CommonOrder order : orders) {
                orderIds.add(order.getId());
            }
            /*if (newOrderList != null) {
                for (CommonOrder i : newOrderList) {
                    orderIds.add(i.getId());
                }
            }*/

            // orderId list can not be empty, else no orders to subscribe to
            if (orderIds.size() != 0) getSubscriberId(orderIds);
        }
    }

    /**
     * Confirm the chosen stand and brand when a new order is made
     *
     * @param newOrder new order
     */
    public void confirmNewOrderStand(CommonOrder newOrder) {
        // Instantiate the RequestQueue
        String chosenStand = newOrder.getStandName();
        String chosenBrand = newOrder.getBrandName();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS;
        url = String.format("%1$s/confirmStand?orderId=%2$s&standName=%3$s&brandName=%4$s",
                url,
                newOrder.getId(),
                chosenStand.replace("&", "%26"),
                chosenBrand.replace("&", "%26"));
        url = url.replace(' ', '+');

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    updateUserOrdersFromDB();
                    showToast("Your order was successfull");
                },
                error -> {
                    showToast("Your final order could not be received");
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
        queue.add(stringRequest);
    }

    /**
     * This method will request orders from the database and update the according dataset for the
     * adapter in the UI
     */
    public void updateUserOrdersFromDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS;
        url = String.format("%1$s/getUserOrders", url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        List<CommonOrder> allUserOrders = mapper.readValue(response.toString(),
                                new TypeReference<List<CommonOrder>>() {
                                });

                        // TODO: Update local database better way @Nathan
                        orderDatabaseService.deleteAllOrders();
                        for (CommonOrder order : allUserOrders) {
                            orderDatabaseService.insertOrder(order);
                        }

                        orders.clear();
                        orders.addAll(allUserOrders);

                        ArrayList<CommonOrder> readyOrders = new ArrayList<CommonOrder>();
                        if (runningOrderSwitch.isChecked()) {
                            for (CommonOrder order : orders) {
                                if (order.getOrderState() == CommonOrder.State.READY) {
                                    readyOrders.add(order);
                                }
                            }
                            orders.removeAll(readyOrders);
                        }


                        Collections.sort(orders, (o1, o2) -> o1.getId() - o2.getId());
                        adapter.notifyDataSetChanged();

                        subscribeToOrderUpdates();

                        showToast("Order updated");
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    showToast("Your orders could not be retrieved from the server");
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

        queue.add(request);
    }


    /**
     * Subscribes to the server eventChannel for the given order
     * and launch the polling service to poll for events (order updates) from the server
     *
     * @param orderId : list of order id's that must be subscribed to
     *                TODO: unregister subscriber
     *                 save subscriberId instead of asking new one every time
     */
    public void getSubscriberId(ArrayList<Integer> orderId) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber?types=o_" + orderId.get(0);
        for (Integer i : orderId.subList(1, orderId.size())) {
            url = url.concat(",o_" + i);
        }

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            subscribeId = Integer.parseInt(response);
            // Start the polling service
            Intent intent = new Intent(getApplicationContext(), PollingService.class);
            intent.putExtra("subscribeId", subscribeId);
            startService(intent);
        }, error -> {
            showToast("Could not subscribe to order updates");
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
        queue.add(stringRequest);
    }

    private void showToast(String message) {
        Toast.makeText(OrderActivity.this, message,
                Toast.LENGTH_SHORT).show();
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
