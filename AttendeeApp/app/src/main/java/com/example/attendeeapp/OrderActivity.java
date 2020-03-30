package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle the show order overview
 */
public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);


        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        return; /*

        ArrayList<ArrayList<MenuItem>> orders = new ArrayList<ArrayList<MenuItem>>();
        ArrayList<MenuItem> newOrder = (ArrayList<MenuItem>) getIntent().getSerializableExtra("order");
        orders.add(newOrder);
        orders.add(newOrder);

        // Initiate the expandable order ListView
        ExpandableListView expandList = (ExpandableListView)findViewById(R.id.order_expand_list);
        OrderExpandableItemAdapter adapter = new OrderExpandableItemAdapter(this, orders);
        expandList.setAdapter(adapter);


        // Instantiate the RequestQueue
        /*RequestQueue queue = Volley.newRequestQueue(this);
        String om_url = "http://cobol.idlab.ugent.be:8091/pingOM";

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, om_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string
                //if (response.length() < 50)  pingText.setText("Response is: " + response);
                //else pingText.setText("Response is: " + response.substring(0, 50));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // Add the request to the RequestQueue
        queue.add(stringRequest);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
}
