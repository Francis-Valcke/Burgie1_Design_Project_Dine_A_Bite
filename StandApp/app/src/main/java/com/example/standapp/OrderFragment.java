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
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.example.standapp.polling.PollingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
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
    private HashMap<String, CommonOrderStatusUpdate.status> listStatus = new HashMap<>();
    private ArrayList<Event> listEvents = new ArrayList<>(); // deprecated

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
                listEvents.clear();
                listOrders.clear();
                listStatus.clear();
                listAdapter.setBrandName(brandName);
                listAdapter.setStandName(standName);
                listAdapter.notifyDataSetChanged();
            }
        }

        // Get already existing orders from server when opening fragment for first time
        if (listDataHeader.isEmpty() && bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            getStandOrders(mContext, user, brandName, standName); // TODO DOES THIS WORK?
        }

        ExpandableListView listView = view.findViewById(R.id.expandable_list_view);
        if (listAdapter == null && bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            listAdapter = new ExpandableListAdapter(listDataHeader, listHash, listEvents,
                    listOrders, listStatus, standName, brandName);
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

    // TODO fix reversing of orders list (graphical glitch?)
    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Event eventUpdate = (Event) intent.getSerializableExtra("eventUpdate");

            if (eventUpdate != null && eventUpdate.getDataType().equals("Order")) {
                // Add objects to the beginning of the ArrayLists
                // -> most recent order at the top of the list on screen
                //listEvents.add(0, eventUpdate);
                //listEvents.add(eventUpdate);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode eventData = eventUpdate.getEventData();
                JsonNode orderJson = eventData.get("order");
                try {
                    CommonOrder orderUpdate = mapper.treeToValue(orderJson, CommonOrder.class);
                    //listOrders.add(0, orderUpdate);
                    listOrders.add(orderUpdate);

                    String orderName = "#" + orderUpdate.getId();
                    //listDataHeader.add(0, orderName);
                    listDataHeader.add(orderName);
                    listStatus.put(orderName, CommonOrderStatusUpdate.status.PENDING);
                    List<String> orderItems = new ArrayList<>();
                    for (CommonOrderItem item : orderUpdate.getOrderItems()) {
                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                    }
                    // Orders should have different order numbers (orderName)
                    listHash.put(orderName, orderItems);
                    listAdapter.notifyDataSetChanged();

                    if (orderUpdate.getOrderState() == CommonOrder.status.PENDING) {
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

    private void getStandOrders(final Context context, final LoggedInUser user, String brandName, String standName) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.OM_ADDRESS + "/getStandOrders?brandName=" + brandName
                + "&standName=" + standName;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println("Received orders from OM: " + response.toString()); // DEBUG
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ArrayList<CommonOrder> allStandOrders = mapper.readValue(response.toString(),
                            new TypeReference<ArrayList<CommonOrder>>() {});
                    for (CommonOrder order : allStandOrders) {
                        listOrders.add(order);
                        String orderName = "#" + order.getId();
                        listDataHeader.add(orderName);
                        listStatus.put(orderName, CommonOrderStatusUpdate.convertStatus(order.getOrderState()));
                        List<String> orderItems = new ArrayList<>();
                        for (CommonOrderItem item : order.getOrderItems()) {
                            orderItems.add(item.getAmount() + " : " + item.getFoodName());
                        }
                        // Orders should have different order numbers (orderName)
                        listHash.put(orderName, orderItems);
                    }
                    listAdapter.notifyDataSetChanged();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); // DEBUG
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
     * @param user logged in user
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
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                System.out.println(response.toString());
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<CommonOrder> orders = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject eventJSON = (JSONObject) response.get(i);
                        Event event = mapper.readValue(eventJSON.toString(), Event.class);
                        if (!event.getDataType().equals("Order")) return;
                        //listEvents.add(0, event);
                        //listEvents.add(event);

                        JSONObject eventData = (JSONObject) eventJSON.get("eventData");
                        JSONObject order = (JSONObject) eventData.get("order");
                        orders.add(mapper.readValue(order.toString(), CommonOrder.class));
                        //listOrders.add(0, mapper.readValue(order.toString(), CommonOrder.class));
                        listOrders.add(mapper.readValue(order.toString(), CommonOrder.class));
                    } catch (JSONException | JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                for (CommonOrder order : orders) {
                    String orderName = "#" + order.getId();
                    //listDataHeader.add(0, orderName);
                    listDataHeader.add(orderName);
                    listStatus.put(orderName, CommonOrderStatusUpdate.status.PENDING);
                    List<String> orderItems = new ArrayList<>();
                    for (CommonOrderItem item : order.getOrderItems()) {
                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                    }
                    // Orders should have different order numbers (orderName)
                    listHash.put(orderName, orderItems);

                    if (order.getOrderState() == CommonOrder.status.PENDING) {
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
