package com.example.standapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.standapp.json.CommonFood;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.example.standapp.polling.PollingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO (optional) change polling to FCM
// TODO or keep a separate order number count per stand?

public class OrderFragment extends Fragment {

    private Context mContext;

    private ExpandableListAdapter listAdapter;
    private ArrayList<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<String>> listHash = new HashMap<>();
    private ArrayList<Event> listEvents = new ArrayList<>();
    private ArrayList<CommonOrder> listOrders = new ArrayList<>();
    private HashMap<String, CommonOrderStatusUpdate.status> listStatus = new HashMap<>();
    private Intent intent;

    // ID from the Event Channel
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

        View view = inflater.inflate(R.layout.activity_order, container, false);

        final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        // Getting the log in information from profile fragment
        final Bundle bundle = getArguments();
        String standName; // DEBUG
        if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            standName = bundle.getString("standName"); // DEBUG
            Toast.makeText(mContext, standName, Toast.LENGTH_SHORT).show(); // DEBUG

            subscriberId = bundle.getString("subscriberId");
        }

        ExpandableListView listView = view.findViewById(R.id.expandable_list_view);
        if (listAdapter == null) listAdapter =
                new ExpandableListAdapter(listDataHeader, listHash, listEvents, listOrders, listStatus);
        listView.setAdapter(listAdapter);

        Button refreshButton = view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                 * This will soon be deleted from the app
                 * because there is now polling of events
                 * It is still here for fall back reasons
                 */

                // Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(mContext);
                String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscriberId;
                System.out.println("Getting orders, URL: " + url);

                // GET request to server
                final JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
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
                                listEvents.add(0, event);

                                JSONObject eventData = (JSONObject) eventJSON.get("eventData");
                                JSONObject order = (JSONObject) eventData.get("order");
                                orders.add(mapper.readValue(order.toString(), CommonOrder.class));
                                listOrders.add(0, mapper.readValue(order.toString(), CommonOrder.class));
                            } catch (JSONException | JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }

                        for (CommonOrder order : orders) {
                            String orderName = "#" + order.getId();
                            listDataHeader.add(0, orderName);
                            listStatus.put(orderName, CommonOrderStatusUpdate.status.PENDING);
                            List<String> orderItems = new ArrayList<>();
                            for (CommonOrderItem item : order.getOrderItems()) {
                                orderItems.add(item.getAmount() + " : " + item.getFoodName());
                            }
                            // Orders should have different order numbers (orderName)
                            listHash.put(orderName, orderItems);

                            // Decrease current stock based on incoming order
                            decreaseStock(order.getOrderItems());
                        }
                        listAdapter.notifyDataSetChanged();
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
                        headers.put("Authorization", user.getAutorizationToken());
                    return headers;
                    }
                };

                queue.add(jsonRequest);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start the polling service
        intent = new Intent(mContext, PollingService.class);
        intent.putExtra("subscribeId", Integer.parseInt(subscriberId));
        mContext.startService(intent); // calls the onStartCommand function of PollingService
        System.out.println("POLLING SERVICE STARTED!");

        // Register the listener for polling updates
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter("eventUpdate"));
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener
        LocalBroadcastManager.getInstance(Objects.requireNonNull(mContext))
                .unregisterReceiver(mMessageReceiver);
        mContext.stopService(intent); // calls the onDestroy() function of PollingService
    }


    // Receives the order updates from the polling service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Event eventUpdate = (Event) intent.getSerializableExtra("eventUpdate");

            if (eventUpdate != null && eventUpdate.getDataType().equals("Order")) {
                // Add objects to the beginning of the ArrayLists
                // -> most recent order at the top of the list on screen
                listEvents.add(0, eventUpdate);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode eventData = eventUpdate.getEventData();
                JsonNode orderJson = eventData.get("order");
                try {
                    CommonOrder orderUpdate = mapper.treeToValue(orderJson, CommonOrder.class);
                    listOrders.add(0, orderUpdate);

                    String orderName = "#" + orderUpdate.getId();
                    listDataHeader.add(0, orderName);
                    listStatus.put(orderName, CommonOrderStatusUpdate.status.PENDING);
                    List<String> orderItems = new ArrayList<>();
                    for (CommonOrderItem item : orderUpdate.getOrderItems()) {
                        orderItems.add(item.getAmount() + " : " + item.getFoodName());
                    }
                    // Orders should have different order numbers (orderName)
                    listHash.put(orderName, orderItems);
                    listAdapter.notifyDataSetChanged();

                    // Decrease current stock based on incoming order
                    decreaseStock(orderUpdate.getOrderItems());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    /**
     * Decrease the current stock values of the menu items of the stand
     * based on incoming orders
     * @param orderItems: menu items of stand that are being ordered
     */
    @SuppressWarnings("unchecked")
    private void decreaseStock(List<CommonOrderItem> orderItems) {
        ArrayList<CommonFood> items;
        Bundle bundle = getArguments();
        if (bundle != null) {
            items = (ArrayList<CommonFood>) bundle.getSerializable("items");
        } else return;

        if (items != null) {
            for (CommonOrderItem orderItem : orderItems) {
                for (CommonFood menuItem : items) {
                    if (orderItem.getFoodName().equals(menuItem.getName())) {
                        menuItem.decreaseStock(orderItem.getAmount());
                    }
                }
            }
        }
    }

}
