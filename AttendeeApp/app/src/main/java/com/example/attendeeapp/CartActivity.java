package com.example.attendeeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import com.example.attendeeapp.json.CommonFood;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

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
        final ArrayList<CommonFood> ordered = (ArrayList<CommonFood>) getIntent().getSerializableExtra("cartList");

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
        for(CommonFood i : ordered) {
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
                    //if(cartAdapter.getCartList().get(0).getStandName().equals("")) {
                        Intent intent = new Intent(CartActivity.this, ConfirmActivity.class);
                        intent.putExtra("order", ordered);
                        intent.putExtra("location", lastLocation);
                        intent.putExtra("totalPrice", totalPrice);
                        intent.putExtra("cartCount", cartAdapter.getCartCount());
                        startActivity(intent);
                    /*} else {



                        Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                        intent.putExtra("order_list", ordered);
                        intent.putExtra("cartCount", cartAdapter.getCartCount());
                        startActivity(intent);
                    }*/

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
                return true;
            case R.id.map_action:
                //User chooses the "Map" item
                Intent mapIntent = new Intent(CartActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
