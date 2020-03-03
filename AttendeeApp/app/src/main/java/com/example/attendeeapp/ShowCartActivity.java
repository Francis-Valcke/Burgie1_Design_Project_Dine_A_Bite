package com.example.attendeeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

//Activity to handle the view cart page
public class ShowCartActivity extends AppCompatActivity {

    ArrayList<MenuItem> ordered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cart);

        ordered = (ArrayList<MenuItem>) getIntent().getSerializableExtra("menuList");

        //Instantiates cart item list
        ListView lView = (ListView)findViewById(R.id.cart_list);
        CartItemAdapter cartAdapter = new CartItemAdapter(ordered, this);
        lView.setAdapter(cartAdapter);

        //Handle TextView to display total cart amount
        TextView total = (TextView)findViewById(R.id.cart_total_price);
        BigDecimal amount = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        for(MenuItem i : ordered) {
            amount = amount.add(i.getPrice().multiply(new BigDecimal((i.getCount()))));
        }
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        total.setText(symbol + amount);

        //Handle clickable TextView to confirm order
        TextView confirm = (TextView)findViewById(R.id.confirm_order);
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //confirm order -> go to order view (test view for now)
                Intent intent = new Intent(ShowCartActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

    }
}
