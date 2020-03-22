package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

/**
 * Activity for handling the global/stand menu view page
 */
public class MenuActivity extends AppCompatActivity implements OnCartChangeListener {

    private static final int MAX_CART_ITEM = 25;
    private ArrayList<MenuItem> cartList = new ArrayList<MenuItem>();
    private int cartCount;
    private Toast mToast = null;

    /**
     * Called after splash-screen is shown
     * Creates menu items view consisting of
     * toolbar, fragment for global menu, fragment for stand menu
     * and cart button with total count
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Create a viewpager to slide between the global and stand menu
        ViewPager2 viewPager = findViewById(R.id.menu_view_pager);
        viewPager.setAdapter(new MenuFragmentAdapter(this));

        // Set up different tabs for the viewpager slider
        TabLayout tabLayout = findViewById(R.id.menu_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText(R.string.tab_global);
                                break;
                            case 1:
                                tab.setText(R.string.tab_stand);
                        }
                    }
                }).attach();

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initializes cart button layout at bottom of menu item list
        TextView totalCount = (TextView)findViewById(R.id.cart_count);
        totalCount.setText("0");

        RelativeLayout relLay = (RelativeLayout)findViewById(R.id.cart_layout);
        relLay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CartActivity.class);
                intent.putExtra("cartList", cartList);
                startActivity(intent);
            }
        });
    }

    /**
     * Updates the cart when a menu item is added
     * If the cart contains the item, increase the count
     * else add the item to the cart
     * If the cart is full (>= MAX_CART_ITEM) or the item has reached it maximum,
     * the item is not added or counted
     * @param cartItem: item to add to the cart with a unique item name
     * TODO: enforce unique name when creating menu items
     */
    public int onCartChangedAdd(MenuItem cartItem) {
        if (cartCount < MAX_CART_ITEM) {
            try {
                boolean contains = false;
                for (MenuItem i : cartList) {
                    if (i.getFoodName().equals(cartItem.getFoodName()) &&
                            i.getStandName().equals(cartItem.getStandName())) {
                        // cartItems have a unique (foodName, standName)
                        i.increaseCount();
                        contains = true;
                        break;
                    }
                }
                if(!contains){
                    cartItem.increaseCount();
                    cartList.add(cartItem);
                }
                cartCount++;
                TextView totalCount = (TextView)findViewById(R.id.cart_count);
                totalCount.setText(String.valueOf(cartCount));

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

    /**
     * Updates the cart when a menu item is removed
     * If the cart contains the item multiple times, decrease the count
     * else if the cart contains it one time, remove the item
     * If the cart is empty the item is not removed and a toast message is shown
     * @param cartItem: item to remove from the cart with a unique item name
     * TODO: enforce unique name when creating menu items
     */
    public int onCartChangedRemove(MenuItem cartItem) {
        if (cartCount > 0) {
            try {
                boolean contains = false;
                for (MenuItem i : cartList) {
                    if (i.getFoodName().equals(cartItem.getFoodName()) &&
                            i.getStandName().equals(cartItem.getStandName())) {
                        // cartItems have a unique (foodName, standName)
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
                TextView totalCount = (TextView)findViewById(R.id.cart_count);
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

