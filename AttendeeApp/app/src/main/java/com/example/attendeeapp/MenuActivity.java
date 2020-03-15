package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity for handling the global menu view page
 */
public class MenuActivity extends AppCompatActivity implements OnCartChangeListener {

    private ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
    private MenuItemAdapter menuAdapter;
    private SwipeRefreshLayout pullToRefresh;
    private Toast mToast;

    /**
     * Called after splash-screen is shown,
     * creates menu items view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Instantiates menu item list
        ListView lView = (ListView)findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, this);
        menuAdapter.setCartChangeListener(this);
        lView.setAdapter(menuAdapter);

        // Setup swipe to refresh menu (e.g. no internet connection)
        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMenu();
                pullToRefresh.setRefreshing(false);
            }
        });

        // Fetch global menu from server
        fetchMenu();

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initializes cart count at bottom of menu item list
        TextView totalCount = (TextView)findViewById(R.id.cart_count);
        totalCount.setText("0");

        RelativeLayout linLay = (RelativeLayout)findViewById(R.id.cart_layout);
        linLay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ShowCartActivity.class);
                intent.putExtra("menuList", menuAdapter.getOrderedMenuList());
                startActivity(intent);
            }
        });
    }

    /**
     * Function to fetch the global menu from the server in JSON
     * Handles no network connection or server not reachable
     * TODO: store menu in cache / fetch menu at splash screen
     */
    private void fetchMenu(){
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://cobol.idlab.ugent.be:8092/menu";

        // Request the global menu in JSON from the stand manager
        // Handle no network connection or server not reachable
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    menuItems.clear();
                    //Log.v("response", "Response: " + response.toString());
                    for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
                        String key = iter.next();
                        String price = response.getString(key);
                        menuItems.add(new MenuItem(key, new BigDecimal(Double.valueOf(price))));
                    }
                    menuAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // NoConnectionError = no network connection
                // other = server not reachable
                if (mToast != null) mToast.cancel();
                if (error instanceof NoConnectionError) {
                    mToast = Toast.makeText(MenuActivity.this, "No network connection", Toast.LENGTH_LONG);

                } else {
                    mToast = Toast.makeText(MenuActivity.this, "Server cannot be reached. Try again later.", Toast.LENGTH_LONG);
                }
                mToast.show();
            }
        });

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Updates total amount (count) in cart when a menu item is added
     * @param cartCount: total number of items in the cart
     */
    public void onCartChanged(int cartCount){
        TextView totalCount = (TextView)findViewById(R.id.cart_count);
        totalCount.setText(String.valueOf(cartCount));
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
            case R.id.orders_action:
                // User chooses the "My Orders" item
                Intent intent = new Intent(MenuActivity.this, OrderActivity.class);
                startActivity(intent);
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

