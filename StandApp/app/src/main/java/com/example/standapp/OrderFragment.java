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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String,List<String>> listHash = new HashMap<>();

    // ID from the event channel
    private String subscriberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_order, container, false);

        // subscribe to the Event Channel
        // so that orders can be received
        // TODO not everytime this fragment is opened/created, only after first logging in
        // TODO when another stands logs in
        subscribeEC();

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
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "http://cobol.idlab.ugent.be:8093/events?id=" + subscriberId;
                System.out.println("LINK: " + "http://cobol.idlab.ugent.be:8093/events?id=" + subscriberId);

                //GET
                final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ObjectMapper mapper = new ObjectMapper();
                        //mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

                        try {
                            String details = (String) response.get("details");
                            JSONArray detailsJSON= new JSONArray(details);

                            for(int i =0 ; i<detailsJSON.length(); i++){
                                JSONObject event= (JSONObject) detailsJSON.get(i);
                                JSONObject eventData= (JSONObject) event.get("eventData");
                                JSONObject orderJson= eventData.getJSONObject("order");
                                CommonOrder order= mapper.readValue(orderJson.toString(), CommonOrder.class);
                                System.out.println(order.toString());

                                String orderName = "Order#" + Integer.toString(order.getId());
                                listDataHeader.add(orderName);
                                List<String> order_details = new ArrayList<>();
                                for (CommonOrderItem j: order.getOrderItems()) {
                                    String detail = Integer.toString(j.getAmount()) + " " + j.getFoodname();
                                    order_details.add(detail);
                                }
                                listHash.put(orderName, order_details);
                            }
                            listAdapter.notifyDataSetChanged();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast mToast = Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer" + " " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0OUlsbAPNyS9_vr-1QcOWnQ");
                        return headers;
                    }
                };
                //Add the request to the RequestQueue
                queue.add(jsonRequest);
            }
        });

        return view;
    }

    /**
     * This function will subscribe to the Event Channel
     */
    private void subscribeEC() {

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        String url = "http://cobol.idlab.ugent.be:8093/registerSubscriber?types=s4"; //TODO: Currently standId 4 is hardcoded, FIX THIS

        //GET
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                Toast mToast = Toast.makeText(getContext(), "SubscriberId: " + response, Toast.LENGTH_SHORT);
                mToast.show();
                subscriberId = response;
                System.out.println("SUBSCRIBEDID: " + subscriberId);
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
                headers.put("Authorization", "Bearer" + " " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi" +
                        "JmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE" +
                        "1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0OUlsbAPNyS9" +
                        "_vr-1QcOWnQ");
                return headers;
            }
        };

        queue.add(request);
    }

}
