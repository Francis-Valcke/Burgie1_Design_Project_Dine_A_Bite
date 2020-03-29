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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFragment extends Fragment {

    private Button refresh;
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String,List<String>> listHash = new HashMap<>();
    //private int standId = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_order, container, false);

        refresh = view.findViewById(R.id.refresh);
        listView = view.findViewById(R.id.expandable_listview);
        //initData();
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listAdapter = new ExpandableListAdapter(listDataHeader,listHash);
        listView.setAdapter(listAdapter);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "http://cobol.idlab.ugent.be:8093/registerSubscriber"; // TODO: fix url

                //GET
                final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        /*if (response == null) {
                            Toast mToast = Toast.makeText(getContext(), "No new orders", Toast.LENGTH_SHORT);
                            mToast.show();
                            return;
                        }*/
                        ObjectMapper mapper = new ObjectMapper();
                        //Map<String, String> headers = jsonRequest.getHeaders();
                        try {
                            CommonOrder order = mapper.readValue(response.get("order").toString(), CommonOrder.class);
                            String orderName = "Order#" + Integer.toString(order.getId());
                            listDataHeader.add(orderName);
                            List<String> order_details = new ArrayList<>();
                            for (CommonOrderItem i: order.getOrderItems()) {
                                String detail = Integer.toString(i.getAmount()) + " " + i.getFoodname();
                                order_details.add(detail);
                            }
                            //order_details.add("2 Fries");
                            //order_details.add("3 Cheeseburgers");

                            listHash.put(orderName, order_details);
                            listAdapter.notifyDataSetChanged();

                        } catch (JsonProcessingException | JSONException e) {
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

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        buttonDashboard = (Button)findViewById(R.id.button_dashboard);
        buttonDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openManagerDashboard();
            }
        });

        listView = (ExpandableListView)findViewById(R.id.expandable_listview);
        initData();
        listAdapter = new ExpandableListAdapter(listDataHeader,listHash);
        listView.setAdapter(listAdapter);
    }*/

    /**
     * Initialize the orders, currently hardcoded, later this will be provided by the server
     */
    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        listDataHeader.add("Order#1");
        listDataHeader.add("Order#2");

        List<String> order1 = new ArrayList<>();
        order1.add("2 Fries");
        order1.add("3 Cheeseburgers");

        listHash.put(listDataHeader.get(0), order1);

        List<String> order2 = new ArrayList<>();
        order2.add("22 Chickennuggets");
        order2.add("5 Pizza Margheritta");
        order2.add("1270 Fanta");

        listHash.put(listDataHeader.get(1), order2);
    }

}
