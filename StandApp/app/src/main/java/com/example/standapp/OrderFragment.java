package com.example.standapp;

import android.content.Context;
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
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO polling of events instead of pressing button
// TODO set/change progress of orders and send to server (in ExpandableListAdapter)
// TODO (optional) change polling to FCM

public class OrderFragment extends Fragment {

    private ExpandableListAdapter listAdapter;
    private Context mContext;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<String>> listHash = new HashMap<>();
    private ArrayList<Event> listEvents = new ArrayList<>();
    private ArrayList<CommonOrder> listOrders = new ArrayList<>();

    // ID from the Event Channel
    private String subscriberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_order, container, false);

        final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        // Getting the log in information from profile fragment
        final Bundle bundle = getArguments();
        String standName;
        if (bundle != null && Utils.isLoggedIn(mContext, bundle)) {
            standName = bundle.getString("standName");
            Toast.makeText(mContext, standName, Toast.LENGTH_SHORT).show();

            subscriberId = bundle.getString("subscriberId");
        }

        // TODO without refresh button, but with automatic polling
        Button refreshButton = view.findViewById(R.id.refresh_button);
        ExpandableListView listView = view.findViewById(R.id.expandable_list_view);
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listAdapter = new ExpandableListAdapter(listDataHeader, listHash, listEvents, listOrders);
        listView.setAdapter(listAdapter);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(mContext));
                String url = ServerConfig.EC_ADDRESS + "/events?id=" + subscriberId;
                System.out.println("URL: " + url);

                // GET request to server
                final JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                        null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response.toString());
                        ObjectMapper mapper = new ObjectMapper();

                        try {
                            List<Event> events = mapper.readValue(response.toString(), new TypeReference<List<Event>>() {});
                            listEvents.addAll(events);
                            ArrayList<CommonOrder> orders = new ArrayList<>();
                            for (Event event : events) {
                                orders.add(mapper.readValue(event.getEventData().get("order").toString(), CommonOrder.class));
                            }
                            listOrders.addAll(orders);
                            for (CommonOrder order : orders) {
                                String orderName = "#" + order.getId();
                                listDataHeader.add(orderName);
                                List<String> orderItems = new ArrayList<>();
                                for (CommonOrderItem item : order.getOrderItems()) {
                                    orderItems.add(item.getAmount() + " : " + item.getFoodName());
                                }
                                listHash.put(orderName, orderItems);
                            }
                            listAdapter.notifyDataSetChanged();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
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
    public void onAttach(@NonNull  Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
