package com.example.attendeeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.attendeeapp.json.CommonFood;
import com.example.attendeeapp.json.CommonOrder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity to handle the view cart page
 */
public class CartActivity extends ToolbarActivity implements AdapterView.OnItemSelectedListener {

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private CartItemAdapter cartAdapter;
    private Toast mToast;
    private Intent returnIntent;
    private BigDecimal totalPrice = new BigDecimal(0);
    private CommonOrder.RecommendType recommendType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

        // Initialize spinner for recommendation type choice
        Spinner spinner = findViewById(R.id.recommend_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recommender_types_list, R.layout.stand_spinner_item);
        adapter.setDropDownViewResource(R.layout.stand_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Get the ordered items from the cart in the menu view (ignore warning)
        final ArrayList<CommonFood> ordered = (ArrayList<CommonFood>) getIntent().getSerializableExtra("cartList");

        // Instantiates cart item list, get the cartCount from menuActivity
        ListView lView = findViewById(R.id.cart_list);
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
        if (ordered != null) {
            for (CommonFood i : ordered) {
                amount = amount.add(i.getPrice().multiply(new BigDecimal((i.getCount()))));
            }
        }
        updatePrice(amount);

        // Handle button to confirm order
        // Only if there are items in the cart, the order can continue
        Button confirmButton = findViewById(R.id.button_confirm_order);
        confirmButton.setOnClickListener(v -> {
            // confirm order -> go to order view
            // Send order with JSON + location
            if (cartAdapter.getCartList().size() > 0) {
                checkLocationPermission();
                //if(cartAdapter.getCartList().get(0).getStandName().equals("")) {
                Intent intent = new Intent(CartActivity.this, ConfirmActivity.class);
                intent.putExtra("order", ordered);
                intent.putExtra("location", lastLocation);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("cartCount", cartAdapter.getCartCount());
                intent.putExtra("recType", recommendType);
                startActivity(intent);
                /*} else {
                    Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                    intent.putExtra("order_list", ordered);
                    intent.putExtra("cartCount", cartAdapter.getCartCount());
                    intent.putExtra("recType", recommendType);
                    startActivity(intent);
                }*/
            } else {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(CartActivity.this, "No items in your cart!",
                        Toast.LENGTH_SHORT);
                mToast.show();
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
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null){
                            lastLocation = task.getResult();
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
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // Create location request to fetch latest user location
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationClient.getLastLocation()
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful() && task.getResult() != null){
                                    lastLocation = task.getResult();
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
    @SuppressLint("SetTextI18n")
    public void updatePrice(BigDecimal amount) {
        TextView total = findViewById(R.id.cart_total_price);
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = Objects.requireNonNull(euro.getCurrency()).getSymbol();
        totalPrice = totalPrice.add(amount);
        total.setText(symbol + totalPrice);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String requestedRecType = (String) parent.getItemAtPosition(position);
        //preprocess string to use valueOf
        if (requestedRecType.equals("Time and distance")) {
            requestedRecType = "DISTANCE_AND_TIME";
        } else {
            requestedRecType = requestedRecType.toUpperCase();
        }
        recommendType = CommonOrder.RecommendType.valueOf(requestedRecType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        recommendType = CommonOrder.RecommendType.DISTANCE_AND_TIME;
    }
}
