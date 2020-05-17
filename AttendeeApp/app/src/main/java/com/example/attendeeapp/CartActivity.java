package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.attendeeapp.json.CommonFood;
import com.example.attendeeapp.json.CommonOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity to handle the cart view user interface.
 * This activity listens for cart updates when food items are added or deleted from the cart.
 */
public class CartActivity extends ToolbarActivity implements AdapterView.OnItemSelectedListener {

    private CartItemAdapter cartAdapter;
    private Toast mToast;
    private Intent returnIntent;
    private BigDecimal totalPrice = new BigDecimal(0);
    private AlertDialog mDialog = null;
    private CommonOrder.RecommendType recommendType;

    /**
     * Method to setup the activity.
     *
     * @param savedInstanceState The previously saved activity state, if available.
     */
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
        // Set default spinner value to time and distance
        String defaultValue = "Time and distance";
        int defaultPosition = adapter.getPosition(defaultValue);
        spinner.setSelection(defaultPosition);
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
                    boolean differentBrands = false;
                    CommonFood firstItem = ordered.get(0);
                    for (CommonFood i : ordered.subList(1, ordered.size())) {
                        if (!i.getBrandName().equals(firstItem.getBrandName())) {
                            // If brand is new alert the user
                            showBrandAlertMessage(ordered);
                            differentBrands = true;
                            break;
                        }
                    }
                    if (!differentBrands) {
                        Intent intent = new Intent(CartActivity.this, ConfirmActivity.class);
                        intent.putExtra("order", ordered);
                        intent.putExtra("totalPrice", totalPrice);
                        intent.putExtra("cartCount", cartAdapter.getCartCount());
                        intent.putExtra("recType", recommendType);
                        startActivity(intent);
                    }

                    //if(cartAdapter.getCartList().get(0).getStandName().equals("")) {

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
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Get most recent location
        checkLocationPermission();
    }

    /**
     * Method that overrides pressing the back button to return the current cart list and count.
     */
    @Override
    public void onBackPressed() {
        returnIntent.putExtra("cartList", cartAdapter.getCartList());
        returnIntent.putExtra("cartCount", cartAdapter.getCartCount());
        super.onBackPressed();
    }

    /**
     * Method to show an alert message when items from different brands are inside the cart,
     * when a user wishes to continue and confirm his cart.
     *
     * @param ordered A list of the current food items in the cart to pass to ConfirmActivity.
     */
    public void showBrandAlertMessage(final ArrayList<CommonFood> ordered) {
        // Alert user if he not better like the recommended stand
        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Continue button
                dialog.cancel();

                // Continue with multiple brands in a split up order
                Intent intent = new Intent(CartActivity.this, ConfirmActivity.class);
                intent.putExtra("order", ordered);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("cartCount", cartAdapter.getCartCount());
                intent.putExtra("recType", recommendType);
                startActivity(intent);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        builder.setMessage("The items in your cart are from multiple brands." +
                "\n\nIf you choose to continue, your order will be split up.")
                .setTitle("Continue with multiple brands");
        if (mDialog != null) mDialog.cancel();
        mDialog = builder.create();
        mDialog.show();
    }

    /**
     * Method to handle price updates when the food items in the cart are updated.
     *
     * @param amount Amount to be added to the current cart price, can be positive or negative.
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

    /**
     * Method that sets the recommendation type when an item is selected from the spinner.
     */
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

    /**
     * When the selection disappears from the spinner, this method sets a default recommendation type
     * to DISTANCE_AND_TIME.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        recommendType = CommonOrder.RecommendType.DISTANCE_AND_TIME;
    }
}
