package com.example.standapp;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<String>> listHash = new HashMap<>();

    // ID from the Event Channel
    private String subscriberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_order, container, false);

        // Getting the log in information from profile fragment
        final Bundle bundle = getArguments();
        String standName = "";
        String brandName = "";
        if (bundle != null && Utils.isLoggedIn(getContext(), bundle)) {
            standName = bundle.getString("standName");
            brandName = bundle.getString("brandName");
            Toast.makeText(getContext(), standName, Toast.LENGTH_SHORT).show();
        }

        // subscribe to the Event Channel and get subscriberID
        // TODO when another stands logs in (erase subscriberID in activity_account?)
        if(subscriberId==null || subscriberId.equals("")){
            subscribeEC(standName, brandName);
        }

        // TODO without refresh button, but with automatic polling
        Button refreshButton = view.findViewById(R.id.refresh_button);
        ExpandableListView listView = view.findViewById(R.id.expandable_list_view);
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listAdapter = new ExpandableListAdapter(listDataHeader, listHash);
        listView.setAdapter(listAdapter);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscriberId;
                System.out.println("URL: " + url);

                // GET request to server
                final JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                        null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response.toString());
                        ObjectMapper mapper = new ObjectMapper();
                        //mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

                        // TODO more checks and balances for exceptions and edge cases
                        try {
                            List<Event> events = mapper.readValue(response.toString(), new TypeReference<List<Event>>() {});
                            System.out.println(events.toString()); // CHECK
                            ArrayList<CommonOrder> orders = new ArrayList<>();
                            for (Event event : events) {
                                String order= event.getEventData().get("order").toString();
                                orders.add(mapper.readValue(event.getEventData().get("order").toString(), CommonOrder.class));
                            }
                            for (CommonOrder order : orders) {
                                String orderName = "Order# " + order.getId();
                                listDataHeader.add(orderName);
                                List<String> orderItems = new ArrayList<>();
                                for (CommonOrderItem item : order.getOrderItems()) {
                                    orderItems.add(item.getAmount() + " " + item.getFoodName());
                                }
                                listHash.put(orderName, orderItems);
                            }
                            listAdapter.notifyDataSetChanged();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        /*
                        try {
                            String details = (String) response.get("details");
                            JSONArray detailsJSON= new JSONArray(details);

                            for (int i =0 ; i < detailsJSON.length(); i++){
                                JSONObject event= (JSONObject) detailsJSON.get(i);
                                JSONObject eventData= (JSONObject) event.get("eventData");
                                JSONObject orderJson= eventData.getJSONObject("order");
                                CommonOrder order= mapper.readValue(orderJson.toString(), CommonOrder.class);
                                System.out.println(order.toString());

                                String orderName = "Order#" + Integer.toString(order.getId());
                                listDataHeader.add(orderName);
                                List<String> order_details = new ArrayList<>();
                                for (CommonOrderItem j: order.getOrderItems()) {
                                    //String detail = Integer.toString(j.getAmount()) + " " + j.getFoodName();
                                    //order_details.add(detail);
                                }
                                listHash.put(orderName, order_details);
                            }
                            listAdapter.notifyDataSetChanged();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                         */
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
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

                queue.add(jsonRequest);
            }
        });

        return view;
    }

    /**
     * This function will subscribe to the Event Channel
     */
    private void subscribeEC(final String standName, final String brandName) {

        // Step 1: Get subscriber ID
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber";

        // GET request to server
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getContext(), "SubscriberId: " + response, Toast.LENGTH_SHORT)
                        .show();
                subscriberId = response;

                // Step 2: Subscribe to stand and subscriberID channels
                System.out.println("SubscriberID = " + subscriberId);
                String url2 = ServerConfig.EC_ADDRESS + "/registerSubscriber/toChannel?type=s_" + standName
                        + "_" + brandName + "&id=" + subscriberId;
                url2 = url2.replace(' ', '+');

                // GET request to server
                final String finalUrl = url2;
                StringRequest request2 = new StringRequest(Request.Method.GET, url2, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response on GET request to " + finalUrl + ": " + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
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
                RequestQueue queue2 = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                queue2.add(request2);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
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

        /*
        // Step 2: Subscribe to stand and subscriberID channels
        String url2 = ServerConfig.EC_ADDRESS + "/registerSubscriber/toChannel?type=s_" + standName
                + "_" + brandName + "&id=" + subscriberId;
        url2 = url2.replace(' ', '+');

        // GET request to server
        StringRequest request2 = new StringRequest(Request.Method.GET, url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                Toast.makeText(getContext(), "SubscriberId: " + response, Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
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

        queue.add(request2);
         */
        /*
         //RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request2 = new StringRequest(Request.Method.GET, url2, future, future) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", ServerConfig.AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        try {
            String response = future.get(30, TimeUnit.SECONDS);
            System.out.println(response);
            Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        */
    }
}
