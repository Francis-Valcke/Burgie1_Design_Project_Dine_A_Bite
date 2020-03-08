package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.math.BigDecimal;
import java.util.ArrayList;

//MainActivity for handling the global menu view page
public class MainActivity extends AppCompatActivity implements OnCartChangeListener {

    private ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

    @Override
    //Called when app is first instantiated, creates menu items view
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuItems.add(new MenuItem("Fries", new BigDecimal(4.10)));
        menuItems.add(new MenuItem("Cheeseburger", new BigDecimal(2.33)));
        menuItems.add(new MenuItem("Pasta", new BigDecimal(5.61)));
        menuItems.add(new MenuItem("Fries", new BigDecimal(4.10)));
        menuItems.add(new MenuItem("Cheeseburger", new BigDecimal(2.33)));
        menuItems.add(new MenuItem("Pasta", new BigDecimal(5.61)));
        menuItems.add(new MenuItem("Fries", new BigDecimal(4.10)));
        menuItems.add(new MenuItem("Cheeseburger", new BigDecimal(2.33)));
        menuItems.add(new MenuItem("Pasta", new BigDecimal(5.61)));
        menuItems.add(new MenuItem("Fries", new BigDecimal(4.10)));
        menuItems.add(new MenuItem("Cheeseburger", new BigDecimal(2.33)));
        menuItems.add(new MenuItem("Pasta", new BigDecimal(5.61)));

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Instantiates menu item list
        ListView lView = (ListView)findViewById(R.id.menu_list);
        final MenuItemAdapter menuAdapter = new MenuItemAdapter(menuItems, this);
        menuAdapter.setCartChangeListener(this);
        lView.setAdapter(menuAdapter);

        //Initializes cart count at bottom of menu item list
        TextView totalCount = (TextView)findViewById(R.id.cart_count);
        totalCount.setText("0");

        RelativeLayout linLay = (RelativeLayout)findViewById(R.id.cart_layout);
        linLay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowCartActivity.class);
                intent.putExtra("menuList", menuAdapter.getOrderedMenuList());
                startActivity(intent);
            }
        });
    }

    //Updates total amount in cart when a menu item is added
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
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
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
