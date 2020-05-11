package com.example.standapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.BetterResponseModel;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.example.standapp.polling.PollingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private Context mContext;

    // List/Maps containing order info
    private ExpandableListAdapter listAdapter;
    private ArrayList<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<String>> listHash = new HashMap<>();
    private ArrayList<CommonOrder> listOrders = new ArrayList<>();
    private HashMap<String, CommonOrderStatusUpdate.State> listStatus = new HashMap<>();

    // Lists containing order info separated into picked up and active
    private ArrayList<String> oldListDataHeader = new ArrayList<>();
    private ArrayList<CommonOrder> oldListOrders = new ArrayList<>();
    private ArrayList<String> activeListDataHeader = new ArrayList<>();
    private ArrayList<CommonOrder> activeListOrders = new ArrayList<>();

    // Polling service
    private Intent intent;

    // Stand name, brand name and ID from the Event Channel
    private String standName;
    private String brandName;
    private String subscriberId;


    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order, container, false);

        final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource())
                .getLoggedInUser();

        final Bundle bundle = getArguments();
        if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            standName = bundle.getString("standName");
            brandName = bundle.getString("brandName");
            subscriberId = bundle.getString("subscriberId");

            Log.d("Order fragment", "Logged in stand: " + standName); // DEBUG
            //Toast.makeText(mContext, standName, Toast.LENGTH_SHORT).show(); // DEBUG

            // Delete data when a different subscriber ID or no ID is detected
            if ((Objects.requireNonNull(bundle.getString("subscriberId")).isEmpty()
                    || !Objects.equals(bundle.getString("subscriberId"), subscriberId))
                    && listAdapter != null) {
                listDataHeader.clear();
                listHash.clear();
                listOrders.clear();
                listStatus.clear();
                oldListDataHeader.clear();
                oldListOrders.clear();
                listAdapter.setBrandName(brandName);
                listAdapter.setStandName(standName);
                listAdapter.notifyDataSetChanged();
            }
        }

        // Get already existing orders from server when opening fragment for first time
        if (activeListDataHeader.isEmpty() && oldListDataHeader.isEmpty()
                && bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            getStandOrders(mContext, user, brandName, standName);
        }

        ExpandableListView listView = view.findViewById(R.id.expandable_list_view);
        if (listAdapter == null && bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            listAdapter = new ExpandableListAdapter(listDataHeader, listHash,
                    listOrders, listStatus, oldListDataHeader, oldListOrders, activeListDataHeader,
                    activeListOrders, standName, brandName);
            listAdapter.setListDataHeader(activeListDataHeader);
            listAdapter.setListOrders(activeListOrders);
        }
        listView.setAdapter(listAdapter);

        Button refreshButton = view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
                    getOrderEvents(mContext, user);
                }
            }
        });

        SwitchMaterial historySwitch = view.findViewById(R.id.switch_history);
        historySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listAdapter.setListDataHeader(activeListDataHeader);
                    listAdapter.setListOrders(activeListOrders);
                } else {
                    listAdapter.setListDataHeader(oldListDataHeader);
                    listAdapter.setListOrders(oldListOrders);
                }
                listAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = getArguments();
        if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            // Start the polling service
            intent = new Intent(mContext, PollingService.class);
            intent.putExtra("subscribeId", Integer.parseInt(subscriberId));
            mContext.startService(intent); // calls the onStartCommand function of PollingService
            System.out.println("POLLING SERVICE STARTED!");

            // Register the listener for polling updates
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                    new IntentFilter("eventUpdate"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Bundle bundle = getArguments();
        if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            // Unregister the listener
            LocalBroadcastManager.getInstance(Objects.requireNonNull(mContext))
                    .unregisterReceiver(mMessageReceiver);

            // Call the onDestroy() function of PollingService
            mContext.stopService(intent);
        }
    }

    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Event eventUpdate = (Event) intent.getSerializableExtra("eventUpdate");

            if (eventUpdate != null && eventUpdate.getDataType().equals("Order")) {
                // Add objects to the beginning of the ArrayLists
                // -> most recent order at the top of the list on screen

                ObjectMapper mapper = new ObjectMapper();
                JsonNode eventData = eventUpdate.getEventData();
                JsonNode orderJson = eventData.get("order");
                try {
                    CommonOrder orderUpdate = mapper.treeToValue(orderJson, CommonOrder.class);
                    activeListOrders.add(0, orderUpdate);

                    String orderName = "#" + orderUpdate.getId();
                    activeListDataHeader.add(0, orderName);
                    listStatus.put(orderName, CommonOrderStatusUpdate.State.PENDING);
                    List<String> orderItems = new ArrayList<>();
                    for (CommonOrderItem item : orderUpdate.getOrderItems()) {
                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                    }
                    // Orders should have different order numbers (orderName)
                    listHash.put(orderName, orderItems);
                    listAdapter.notifyDataSetChanged();

                    if (orderUpdate.getOrderState() == CommonOrder.State.PENDING) {
                        // Update revenue
                        RevenueViewModel model = new ViewModelProvider(requireActivity())
                                .get(RevenueViewModel.class);
                        model.updateRevenue(orderUpdate.getOrderItems());

                        // Decrease current stock based on incoming order
                        MenuViewModel menuViewModel = new ViewModelProvider(requireActivity())
                                .get(MenuViewModel.class);
                        menuViewModel.decreaseStock(orderUpdate.getOrderItems());
                    }

                    Log.d("Order fragment", "Received a new order");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    /**
     * Get the orders of the logged in stand from the server after logging back in
     * or opening the app after it being force closed
     *
     * @param context   context from which the method is called
     * @param user      logged in user
     * @param brandName brand name of logged in stand
     * @param standName stand name of logged in stand
     */
    private void getStandOrders(final Context context, final LoggedInUser user, String brandName, String standName) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.OM_ADDRESS + "/getStandOrders?brandName=" + brandName
                + "&standName=" + standName;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Received orders from OM: " + response.toString()); // DEBUG
                        ObjectMapper mapper = new ObjectMapper();

                        BetterResponseModel<List<CommonOrder>> responseModel = null;
                        try {
                            responseModel = mapper
                                    .readValue(response.toString(), new TypeReference<BetterResponseModel<List<CommonOrder>>>() {
                                    });
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error parsing response while fetching orders", Toast.LENGTH_LONG).show();
                        }


                        if (responseModel != null) {
                            if (responseModel.isOk()) {
                                for (CommonOrder order : responseModel.getPayload()) {
                                    String orderName = "#" + order.getId();
                                    if (order.getOrderState() == CommonOrder.State.PICKED_UP) {
                                        oldListOrders.add(0, order);
                                        oldListDataHeader.add(0, orderName);
                                    } else {
                                        activeListOrders.add(0, order);
                                        activeListDataHeader.add(0, orderName);
                                    }
                                    //listOrders.add(0, order);
                                    //listDataHeader.add(0, orderName);
                                    listStatus.put(orderName, CommonOrderStatusUpdate.convertStatus(order.getOrderState()));
                                    List<String> orderItems = new ArrayList<>();
                                    for (CommonOrderItem item : order.getOrderItems()) {
                                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                                    }
                                    // Orders should have different order numbers (orderName)
                                    listHash.put(orderName, orderItems);
                                }
                                listAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Error while fetching stand orders", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(context, "Error while fetching stand orders", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show(); // DEBUG
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(request);
    }

    /**
     * Get orders from Event Channel
     *
     * @param context context from which method is called
     * @param user    logged in user
     */
    private void getOrderEvents(final Context context, final LoggedInUser user) {

        // Class variables used:
        // - subscriber ID
        // - list adapter
        // - all the order lists

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscriberId;
        System.out.println("Getting orders, URL: " + url);

        // GET request to server
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<CommonOrder> orders = new ArrayList<>();

                // map to betterresponsemodel
                BetterResponseModel<List<Event>> responseModel=null;
                try {
                    responseModel = mapper
                            .readValue(response.toString(), new TypeReference<BetterResponseModel<List<Event>>>() {
                            });
                }
                catch (JsonProcessingException e){
                    e.printStackTrace();
                    Toast.makeText(context,"Error while parsing response for getting orders", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Event event : responseModel.getPayload()) {
                    try {
                        if (!event.getDataType().equals("Order")) return;

                        JsonNode eventData = event.getEventData();
                        JsonNode order = eventData.get("order");
                        orders.add(mapper.readValue(order.toString(), CommonOrder.class));
                        activeListOrders.add(0, mapper.readValue(order.toString(), CommonOrder.class));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                for (CommonOrder order : orders) {
                    String orderName = "#" + order.getId();
                    activeListDataHeader.add(0, orderName);
                    listStatus.put(orderName, CommonOrderStatusUpdate.State.PENDING);
                    List<String> orderItems = new ArrayList<>();
                    for (CommonOrderItem item : order.getOrderItems()) {
                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                    }
                    // Orders should have different order numbers (orderName)
                    listHash.put(orderName, orderItems);

                    if (order.getOrderState() == CommonOrder.State.PENDING) {
                        // Update revenue
                        RevenueViewModel revenueViewModel = new ViewModelProvider(requireActivity())
                                .get(RevenueViewModel.class);
                        revenueViewModel.updateRevenue(order.getOrderItems());

                        // Decrease current stock based on incoming order
                        MenuViewModel menuViewModel = new ViewModelProvider(requireActivity())
                                .get(MenuViewModel.class);
                        menuViewModel.decreaseStock(order.getOrderItems());
                    }
                }
                listAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(jsonRequest);
    }

}
