package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.example.attendeeapp.json.CommonFood;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

/**
 * Activity for handling the global/stand menu user interface.
 * This activity listens for updates when food items are added or deleted from the cart.
 */
public class MenuActivity extends ToolbarActivity implements OnCartChangeListener {

    private static final int MAX_CART_ITEM = 25;
    private ArrayList<CommonFood> cartList = new ArrayList<>();
    private int cartCount;
    private Toast mToast = null;
    private AlertDialog mDialog = null;

    /**
     * Called after splash-screen is shown.
     * <p>
     * Creates the menu view consisting of
     * a toolbar, fragment for global menu, fragment for stand menu, fragment for category menu
     * and a cart button with the total cart count.
     *
     * @param savedInstanceState The previously saved activity state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Initialize the toolbar
        initToolbar();

        // Create a viewpager to slide between the global and stand menu
        ViewPager2 viewPager = findViewById(R.id.menu_view_pager);
        viewPager.setAdapter(new MenuFragmentAdapter(this));

        // Set up different tabs for the viewpager slider
        TabLayout tabLayout = findViewById(R.id.menu_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.tab_global);
                            break;
                        case 1:
                            tab.setText(R.string.tab_stand);
                            break;
                        case 2:
                            tab.setText(R.string.tab_category);
                            break;
                    }
                }).attach();

        // Initializes cart button layout at bottom of menu item list
        TextView totalCount = findViewById(R.id.cart_count);
        totalCount.setText("0");

        RelativeLayout relLay = findViewById(R.id.cart_layout);
        relLay.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CartActivity.class);
            intent.putExtra("cartList", cartList);
            intent.putExtra("cartCount", cartCount);
            startActivityForResult(intent, 1);
        });
    }

    /**
     * Called after onCreate().
     */
    @Override
    public void onStart() {
        super.onStart();
        // Ask for location permission
        checkLocationPermission();
    }

    /**
     * Called when the result from CartActivity is available to update the current cart for this activity.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                // Ignore warning
                cartList = (ArrayList<CommonFood>) data.getSerializableExtra("cartList");
                cartCount = data.getIntExtra("cartCount", 0);
                TextView totalCount = findViewById(R.id.cart_count);
                totalCount.setText("" + cartCount);
            }

        }
    }

    /**
     * Method to alert the user when he tries to add items to his cart from a different brand
     * than the ones he already has.
     *
     * @param cartItem The food item the user wishes to add to his cart.
     */
    public void showBrandAlertMessage(final CommonFood cartItem) {
        // Alert user if he not better like the recommended stand
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Add button
                dialog.cancel();

                // Continue with multiple brands in a split up order
                CommonFood newItem = new CommonFood(cartItem);
                newItem.increaseCount();
                cartList.add(newItem);
                cartCount++;
                TextView totalCount = findViewById(R.id.cart_count);
                totalCount.setText(String.valueOf(cartCount));

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        builder.setMessage("The selected item is from another brand than the one(s) currently in your cart." +
                "\n\nIf you choose to add this item, your order will be SPLIT UP." +
                "\nAre you sure you want add this item?")
                .setTitle("Add item from another brand");
        if (mDialog != null) mDialog.cancel();
        mDialog = builder.create();
        mDialog.show();
    }

    // TODO: enforce unique name when creating menu items
    /**
     * Updates the cart when a menu item is added.
     * <p>
     * If the cart contains the food item, increase the count
     * else add the item to the cart.
     * If the cart is full (>= MAX_CART_ITEM) or the item has reached it maximum,
     * the item is not added or counted.
     *
     * @param cartItem Food item to add to the cart.
     * @return The (updated) cartCount.
     */
    public int onCartChangedAdd(CommonFood cartItem) {
        if (cartCount < MAX_CART_ITEM) {
            try {
                boolean contains = false;
                boolean newBrand = true;
                for (CommonFood i : cartList) {
                    if (i.getName().equals(cartItem.getName()) &&
                            i.getStandName().equals(cartItem.getStandName()) &&
                            i.getBrandName().equals(cartItem.getBrandName())) {
                        // cartItems have a unique ((foodName, brandName), standName)
                        i.increaseCount();
                        contains = true;
                        newBrand = false;
                        break;
                    }
                }
                if(!contains){
                    // If order is from another brand, notify the user
                    for (CommonFood i : cartList) {
                        if (i.getBrandName().equals(cartItem.getBrandName())) {
                            newBrand = false;
                            break;
                        }
                    }
                    if (cartList.size() == 0) newBrand = false;
                    if (!newBrand) {
                        CommonFood newItem = new CommonFood(cartItem);
                        newItem.increaseCount();
                        cartList.add(newItem);
                    } else {
                        // If brand is new alert the user
                        showBrandAlertMessage(cartItem);
                    }
                }
                if (!newBrand) {
                    cartCount++;
                    TextView totalCount = findViewById(R.id.cart_count);
                    totalCount.setText(String.valueOf(cartCount));
                }

            } catch (ArithmeticException e){
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(this,"No more than 10 items",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        } else {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(this,"No more than 25 in total",
                    Toast.LENGTH_SHORT);
            mToast.show();
        }
        return cartCount;
    }

    // TODO: enforce unique name when creating menu items
    /**
     * Updates the cart when a menu item is removed.
     * <p>
     * If the cart contains the food item multiple times, decrease the count
     * else if the cart contains it one time, remove the item.
     * If the cart is empty the item is not removed and a toast message is shown.
     *
     * @param cartItem Food item to remove from the cart.
     * @return The (updated) cartCount.
     */
    public int onCartChangedRemove(CommonFood cartItem) {
        if (cartCount > 0) {
            try {
                boolean contains = false;
                for (CommonFood i : cartList) {
                    if (i.getName().equals(cartItem.getName()) &&
                            i.getStandName().equals(cartItem.getStandName()) &&
                            i.getBrandName().equals(cartItem.getBrandName())) {
                        // cartItems have a unique ((foodName, brandName), standName)
                        i.decreaseCount();
                        if (i.getCount() == 0){
                            cartList.remove(i);
                        }
                        contains = true;
                        break;
                    }
                }
                if(!contains){
                    throw new ArithmeticException("This menuItem is not in the cart!");
                }
                cartCount--;
                TextView totalCount = findViewById(R.id.cart_count);
                totalCount.setText(String.valueOf(cartCount));

            } catch (ArithmeticException e){
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(this,"This item is no longer in your cart",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        } else {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(this,"No more items in your cart",
                    Toast.LENGTH_SHORT);
            mToast.show();
        }
        return cartCount;
    }

}

