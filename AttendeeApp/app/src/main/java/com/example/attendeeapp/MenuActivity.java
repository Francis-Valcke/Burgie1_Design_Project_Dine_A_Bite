package com.example.attendeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.attendeeapp.json.CommonFood;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

/**
 * Activity for handling the global/stand menu view page
 */
public class MenuActivity extends AppCompatActivity implements OnCartChangeListener {

    private static final int MAX_CART_ITEM = 25;
    private ArrayList<CommonFood> cartList = new ArrayList<>();
    private int cartCount;
    private Toast mToast = null;

    /**
     * Called after splash-screen is shown
     * Creates menu items view consisting of
     * toolbar, fragment for global menu, fragment for stand menu
     * and cart button with total count
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initializes cart button layout at bottom of menu item list
        TextView totalCount = findViewById(R.id.cart_count);
        totalCount.setText("0");

        RelativeLayout relLay = findViewById(R.id.cart_layout);
        relLay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CartActivity.class);
                intent.putExtra("cartList", cartList);
                intent.putExtra("cartCount", cartCount);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                // Ignore warning
                cartList = (ArrayList<CommonFood>) data.getSerializableExtra("cartList");
                cartCount = data.getIntExtra("cartCount", 0);
                TextView totalCount = (TextView)findViewById(R.id.cart_count);
                totalCount.setText("" + cartCount);
            }

        }
    }

    /**
     * Updates the cart when a menu item is added
     * If the cart contains the item, increase the count
     * else add the item to the cart
     * If the cart is full (>= MAX_CART_ITEM) or the item has reached it maximum,
     * the item is not added or counted
     * @param cartItem: item to add to the cart with a unique item name
     * @return the (updated) cartCount
     * TODO: enforce unique name when creating menu items
     */
    public int onCartChangedAdd(CommonFood cartItem) {
        if (cartCount < MAX_CART_ITEM) {
            try {
                boolean contains = false;
                for (CommonFood i : cartList) {
                    if (i.getName().equals(cartItem.getName()) &&
                            i.getStandName().equals(cartItem.getStandName()) &&
                            i.getBrandName().equals(cartItem.getBrandName())) {
                        // cartItems have a unique ((foodName, brandName), standName)
                        i.increaseCount();
                        contains = true;
                        break;
                    }
                }
                if(!contains){
                    CommonFood newItem = new CommonFood(cartItem);
                    newItem.increaseCount();
                    cartList.add(newItem);
                }
                cartCount++;
                TextView totalCount = findViewById(R.id.cart_count);
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
     * @return the (updated) cartCount
     * TODO: enforce unique name when creating menu items
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // TODO:
    //  -make toolbar generalized for all activities
    //  -make Toast messages cancalable for all activities
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
                return true;
            case R.id.map_action:
                //User chooses the "Map" item
                Intent mapIntent = new Intent(MenuActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

