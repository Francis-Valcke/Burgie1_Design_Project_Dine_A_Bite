package com.example.attendeeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

}
