package com.example.standapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ExpandableListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button buttonDashboard;
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;

    /**
     * This onCreate function is the first function that will be run when the MainActivity opens up
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonDashboard = (Button) findViewById(R.id.button_dashboard);
        buttonDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openManagerDashboard();
            }
        });

        listView = (ExpandableListView)findViewById(R.id.expandable_listview);
        initData();
        listAdapter = new ExpandableListAdapter(listDataHeader,listHash);
        listView.setAdapter(listAdapter);
    }

    /**
     * Initialize the orders, currently hardcoded, later this will be provided by the server
     */
    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        listDataHeader.add("Order#1");
        listDataHeader.add("Order#2");

        List<String> order1 = new ArrayList<>();
        order1.add("2 Fries");
        order1.add("3 Cheeseburgers");

        listHash.put(listDataHeader.get(0), order1);

        List<String> order2 = new ArrayList<>();
        order2.add("22 Chickennuggets");
        order2.add("5 Pizza Margheritta");
        order2.add("1270 Fanta");

        listHash.put(listDataHeader.get(1), order2);
        //listView.expandGroup(0);
    }

    /**
     * Opens up the dashboard activity
     */
    public void openManagerDashboard() {
        Intent intent = new Intent(this, ManagerDashboard.class);
        startActivity(intent);
    }
}
