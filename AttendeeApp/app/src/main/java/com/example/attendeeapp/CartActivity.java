package com.example.attendeeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.order.CommonOrder;
import com.example.attendeeapp.order.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to handle the view cart page
 */
public class CartActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private CartItemAdapter cartAdapter;
    private Toast mToast;
    private Intent returnIntent;
    private BigDecimal totalPrice = new BigDecimal(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the ordered items from the cart in the menu view
        ArrayList<MenuItem> ordered = (ArrayList<MenuItem>) getIntent().getSerializableExtra("cartList");

        // Instantiates cart item list, get the cartCount from menuActivity
        ListView lView = (ListView)findViewById(R.id.cart_list);
        cartAdapter = new CartItemAdapter(ordered, this);
        cartAdapter.setCartCount(getIntent().getIntExtra("cartCount", 0));
        lView.setAdapter(cartAdapter);

        // Set up the returning possible edited cart list and count
        returnIntent = new Intent();
        returnIntent.putExtra("cartList", cartAdapter.getCartList());
        returnIntent.putExtra("cartCount", cartAdapter.getCartCount());
        setResult(RESULT_OK, returnIntent);


        // Handle TextView to display total cart amount (price)
        BigDecimal amount = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        for(MenuItem i : ordered) {
            amount = amount.add(i.getPrice().multiply(new BigDecimal((i.getCount()))));
        }
        updatePrice(amount);

        // Handle clickable TextView to confirm order
        // Only if there are items in the cart, the order can continue
        TextView confirm = (TextView)findViewById(R.id.confirm_order);
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // confirm order -> go to order view (test view for now)
                // Send order with JSON + location
                if (cartAdapter.getCartList().size() > 0) {
                    checkLocationPermission();
                    requestOrderRecommend();
                    Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                    startActivity(intent);
                } else {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(CartActivity.this, "No items in your cart!",
                                            Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });

    }

    /**
     * Called after onCreate()
     */
    @Override
    public void onStart() {
        super.onStart();
        // Ask for location permission
        checkLocationPermission();
    }

    @Override
    public void onBackPressed() {
        returnIntent.putExtra("cartList", cartAdapter.getCartList());
        returnIntent.putExtra("cartCount", cartAdapter.getCartCount());
        super.onBackPressed();
    }

    /**
     * Check if location permission is granted
     * It not: request the location permission
     * else if permission was granted, renew user location
     */
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // Request the latest user location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(Task<Location> task ) {
                            if(task.isSuccessful() && task.getResult() != null){
                                lastLocation = task.getResult();
                            }
                        }
                    });
        }
    }


    /**
     * Handle the requested permissions,
     * here only the location permission is handled
     * @param requestCode: 1 = location permission was requested
     * @param permissions: the requested permission(s) names
     * @param grantResults: if the permission is granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // Create location request to fetch latest user location
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationClient.getLastLocation()
                            .addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(Task<Location> task ) {
                                    if(task.isSuccessful() && task.getResult() != null){
                                        lastLocation = task.getResult();
                                    }
                                }
                            });
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Function to handle price updates when the cart updates its items
     * @param amount: amount to be added, can be positive or negative
     */
    public void updatePrice(BigDecimal amount) {
        TextView total = (TextView)findViewById(R.id.cart_total_price);
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        totalPrice = totalPrice.add(amount);
        total.setText(symbol + totalPrice);
    }


    /**
     * Sends order of user to the server in JSON to request a recommendation
     * Send a JSON object with ordered items and user location
     * Format: Order converted to JSON
     * Location is (360, 360) when user location is unknown
     */
    private void requestOrderRecommend() {

        //360 is value for location unknown
        double latitude = 360;
        double longitude = 360;
        if(lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        // Make JSON Object with ordered items and location
        CommonOrder order = new CommonOrder(cartAdapter.getCartList(), cartAdapter.getCartList().get(0).getStandName(), cartAdapter.getCartList().get(0).getBrandName(), latitude, longitude);
        JSONObject jsonOrder = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonOrderString = mapper.writeValueAsString(order);
            jsonOrder = new JSONObject(jsonOrderString);

        } catch (JsonProcessingException | JSONException e) {
            Log.v("JsonException in cart", e.toString());
        }

        // remove unnecessary initial values, this will be set by server
        jsonOrder.remove("id");
        jsonOrder.remove("startTime");
        jsonOrder.remove("expectedTime");
        jsonOrder.remove("standId");



        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url = "http://10.0.2.2:8081/placeOrder";
        String url = "http://cobol.idlab.ugent.be:8091/placeOrder";


        // Request recommendation from server for sent order (both in JSON)
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonOrder,
                                                            new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast mToast = Toast.makeText(CartActivity.this, "Ordering successful!",
                                                Toast.LENGTH_SHORT);

                ObjectMapper mapper= new ObjectMapper();
                try {
                    List<Recommendation> recommendations= mapper.readValue(response.get("recommendations").toString(), new TypeReference<List<Recommendation>>() {});
                    CommonOrder order= mapper.readValue(response.get("order").toString(), CommonOrder.class);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mToast.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast mToast = Toast.makeText(CartActivity.this, "Ordering failed",
                                                Toast.LENGTH_SHORT);
                mToast.show();
            }
        }) { // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders()  throws AuthFailureError {
                Map<String, String>  headers  = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi" +
                        "JmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYX" +
                        "QiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0O" +
                        "UlsbAPNyS9_vr-1QcOWnQ");
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
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
                onBackPressed();
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                Intent intent = new Intent(CartActivity.this, OrderActivity.class);
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
