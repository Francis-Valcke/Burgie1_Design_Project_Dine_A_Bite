package com.example.standapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderFragment extends Fragment {
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_order, container, false);

        listView = (ExpandableListView)view.findViewById(R.id.expandable_listview);
        initData();
        listAdapter = new ExpandableListAdapter(listDataHeader,listHash);
        listView.setAdapter(listAdapter);
        return view;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        buttonDashboard = (Button)findViewById(R.id.button_dashboard);
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
    }*/

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
    /*public void openManagerDashboard() {
        Intent intent = new Intent(this, DashboardFragment.class);
        startActivity(intent);
    }*/
}
