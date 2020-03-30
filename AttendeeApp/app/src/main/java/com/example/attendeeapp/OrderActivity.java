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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.order.CommonOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ArrayList<ArrayList<MenuItem>> orders = new ArrayList<ArrayList<MenuItem>>();
        ArrayList<MenuItem> newOrder_list = (ArrayList<MenuItem>) getIntent().getSerializableExtra("order_list");
        final CommonOrder newOrder = (CommonOrder) getIntent().getSerializableExtra("order");
        int stand = getIntent().getIntExtra("standID", 0);

        if(newOrder_list == null) {
            // TODO: loading stored orders not yet handled
            return;
        }
        orders.add(newOrder_list);

        // Initiate the expandable order ListView
        ExpandableListView expandList = (ExpandableListView)findViewById(R.id.order_expand_list);
        OrderExpandableItemAdapter adapter = new OrderExpandableItemAdapter(this, orders);
        adapter.setOrderId(newOrder.getId());
        adapter.setOrderCount(getIntent().getIntExtra("cartCount", 0));
        BigDecimal amount = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        for(MenuItem i : newOrder_list) {
            amount = amount.add(i.getPrice().multiply(new BigDecimal((i.getCount()))));
        }
        adapter.setTotalPrice(amount);
        expandList.setAdapter(adapter);


        // Request the order ID from the server and confirm the chosen stand.

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String om_url = String.format("http://cobol.idlab.ugent.be:8091/confirmStand?order_id=%1$s&stand_id=%2$s", newOrder.getId(), stand);

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, om_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Your order was successful",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast mToast = null;
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(OrderActivity.this, "Your order could not be received",
                        Toast.LENGTH_SHORT);
                mToast.show();

            }
        }) {
            // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders()  throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi" +
                        "JmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYX" +
                        "QiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0O" +
                        "UlsbAPNyS9_vr-1QcOWnQ");
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon
                // on the main android toolbar.
                Intent intent = new Intent(OrderActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                return true;
            case R.id.account_action:
                // User chooses the "Account" item
                // TODO make account activity
                return true;
            case R.id.settings_action:
                // User chooses the "Settings" item
                // TODO make settings activity
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
