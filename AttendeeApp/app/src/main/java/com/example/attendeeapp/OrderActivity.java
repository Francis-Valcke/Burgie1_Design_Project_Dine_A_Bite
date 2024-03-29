package com.example.attendeeapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.os.Looper;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderStatusUpdate;
import com.example.attendeeapp.polling.PollingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to handle the order user interface.
 */
public class OrderActivity extends ToolbarActivity {

    private int subscribeId = -1;
    private Switch runningOrderSwitch;
    private ArrayList<CommonOrder> orders;
    private OrderDatabaseService orderDatabaseService;
    private OrderItemExpandableAdapter adapter;
    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    private Location lastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    /**
     * Receives the order updates from the polling service.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CommonOrder orderUpdate = (CommonOrder) intent.getSerializableExtra("orderUpdate");
            CommonOrderStatusUpdate orderStatusUpdate = (CommonOrderStatusUpdate) intent.getSerializableExtra("orderStatusUpdate");
            if (orderUpdate != null) {
                // Update all order fields
                adapter.updateOrder(orderUpdate);
                adapter.notifyDataSetChanged();
            }
            if (orderStatusUpdate != null) {
                // Update order status fields
                adapter.updateOrderState(orderStatusUpdate.getOrderId(), orderStatusUpdate.getNewState());
                adapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * If the service polling for order updates is currently running or not
     */
    private boolean isPollingServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to setup the activity.
     *
     * @param savedInstanceState The previously saved activity state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // -- init UI -- //
        setContentView(R.layout.activity_order);
        initToolbar();
        upButtonToolbar();

        // Initialize variables to enable location update requests
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100); // set the interval in which you want to receive locations
        locationRequest.setFastestInterval(10); // if a location is available sooner you can get it faster

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    System.out.println("Location is NULL!");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastLocation = location;
                    System.out.println("MY LOCATION: latitude: " + location.getLatitude() + "; longitude: " + location.getLongitude());
                }
            }
        };
        startLocationUpdates();

        // order(s) passed by confirm order activity
        ArrayList<CommonOrder> newOrderList = (ArrayList<CommonOrder>) getIntent().getSerializableExtra("orderList");

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
        // Start the location updates
        startLocationUpdates();
        // Start polling service
        if (subscribeId != -1) {
            Intent intent = new Intent(getApplicationContext(), PollingService.class);
            intent.putExtra("subscribeId", subscribeId);
            intent.putExtra("locations", getMapIdLocation());
            intent.putExtra("remainingTime", getMapIdRemainingTime());
            intent.putExtra("myLocationLat", lastLocation.getLatitude());
            intent.putExtra("myLocationLon", lastLocation.getLongitude());
            startService(intent);
        }
        // Register the listener for polling updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("orderUpdate"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the location updates
        stopLocationUpdates();
        // Stop the polling service
        stopService(new Intent(getApplicationContext(), PollingService.class));

        // Unregister the listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Method to check if the polling service is running
     * and if not request a new subscriberId from the server for the current orders.
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
     * This method will confirm the chosen stand and brand for an order at the server.
     *
     * @param newOrder The new order that must be confirmed.
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

        // Request a JsonObjectRequest response from the provided URL
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    BetterResponseModel<String> responseModel = null;
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        responseModel = mapper
                                .readValue(response.toString(), new TypeReference<BetterResponseModel<String>>() {
                                });
                    } catch (JsonProcessingException e) {
                        Log.v("JSON exception", "JSON exception in confirmActivity");
                        e.printStackTrace();
                        showToast("Exception while parsing response for confirming order");
                        return;
                    }
                    if (responseModel != null) {
                        if (responseModel.isOk()) {
                            updateUserOrdersFromDB();
                            showToast("Your order was successful");
                        } else {
                            showToast(responseModel.getException().getMessage());
                        }
                    } else {
                        showToast("Exception while receiving response from confirming order");
                    }
                }, error -> {
            showToast("Order could not be confirmed");
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
        queue.add(jsonRequest);
    }

    /**
     * This method will request orders from the server's database and update the according dataset for the
     * adapter in the UI.
     */
    public void updateUserOrdersFromDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS;
        url = String.format("%1$s/getUserOrders", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    ObjectMapper mapper = new ObjectMapper();

                    BetterResponseModel<List<CommonOrder>> responseModel = null;
                    try {
                        responseModel = mapper.readValue(response.toString(), new TypeReference<BetterResponseModel<List<CommonOrder>>>() {
                        });
                    } catch (JsonProcessingException e) {
                        showToast("Error while parsing orders");
                        return;
                    }

                    if (responseModel.isOk()) {
                        List<CommonOrder> allUserOrders = responseModel.getPayload();
                        // List to keep track of orders with stand and brand != ""
                        ArrayList<CommonOrder> nonEmptyOrders = new ArrayList<>();
                        // TODO: Update local database better way @Nathan
                        // TODO: Loads previous orders from the internal database stored on the device
                        orderDatabaseService.deleteAllOrders();
                        for (CommonOrder order : allUserOrders) {
                            // TODO solve this, unconfirmed orders give an error when placing in local db
                            if (order.getRecType() != null && !order.getBrandName().equals("")
                                    && !order.getStandName().equals("")) {
                                nonEmptyOrders.add(order);
                                orderDatabaseService.insertOrder(order);
                            }
                        }

                        orders.clear();
                        orders.addAll(nonEmptyOrders);

                        ArrayList<CommonOrder> pickedUpOrders = new ArrayList<>();
                        if (runningOrderSwitch.isChecked()) {
                            for (CommonOrder order : orders) {
                                if (order.getOrderState() == CommonOrder.State.PICKED_UP) {
                                    pickedUpOrders.add(order);
                                }
                            }
                            orders.removeAll(pickedUpOrders);
                        }


                        Collections.sort(orders, (o1, o2) -> o1.getId() - o2.getId());
                        adapter.notifyDataSetChanged();

                        subscribeToOrderUpdates();
                    } else {
                        showToast(responseModel.getDetails());
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

    // TODO: unregister subscriber
    // TODO: save subscriberId instead of asking new one every time
    /**
     * Method to subscribe to the server EventChannel for the given orders of the list
     * and launch the polling service to poll for events (order updates for these orders) from the server.
     *
     * @param orderId List of order id's that must be subscribed to.
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
            intent.putExtra("locations", getMapIdLocation());
            intent.putExtra("remainingTime", getMapIdRemainingTime());
            assert lastLocation != null;
            intent.putExtra("myLocationLat", lastLocation.getLatitude());
            intent.putExtra("myLocationLon", lastLocation.getLongitude());
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

    /**
     * Method to display a toast with a specific message.
     *
     * @param message The message to show in the Toast.
     */
    private void showToast(String message) {
        Toast.makeText(OrderActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * This function will return a hashmap, mapping the ID of the order to its location
     * @return hashmap mapping the ID of the order to its location
     */
    private HashMap<Integer, LatLng> getMapIdLocation() {
        HashMap<Integer, LatLng> result = new HashMap<>();
        for (CommonOrder order : orders) {
            LatLng loc = new LatLng(order.getLatitude(), order.getLongitude());
            result.put(order.getId(), loc);
        }
        return result;
    }

    /**
     * This function will return a hashmap, mapping the ID of the order to its remaining time
     * @return hashmap mapping the ID of the order to its remaining time
     */
    private HashMap<Integer, Integer> getMapIdRemainingTime() {
        HashMap<Integer, Integer> result = new HashMap<>();
        for (CommonOrder order : orders) {
            result.put(order.getId(), order.computeRemainingTime()/1000);
        }
        return result;
    }


    /**
     * Method that overrides pressing the back button to return to the MenuActivity
     * instead of the ConfirmActivity.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    /**
     * Extends the toolbar option selection to exclude the my order selection button.
     *
     * @param item The selected item in the toolbar menu.
     * @return If the click event should be consumed or forwarded.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.orders_action) {
            // User chooses the "My Orders" item
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
