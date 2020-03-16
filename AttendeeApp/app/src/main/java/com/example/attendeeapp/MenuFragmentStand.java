package com.example.attendeeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

/**
 * Handles the view for the stand menu's
 */
public class MenuFragmentStand extends MenuFragment implements AdapterView.OnItemSelectedListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_stand, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Hardcoded stand names
        ArrayList<String> standList = new ArrayList<String>();
        standList.add("food1");
        standList.add("food2");

        // Create a spinner item for the different stands
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.stand_spinner_item, standList);
        spinner.setAdapter(adapter);

        // Instantiates menu item list
        ListView lView = (ListView) view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);

        // Setup swipe to refresh menu (e.g. no internet connection)
        pullToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMenu("food1");
                pullToRefresh.setRefreshing(false);
            }
        });

        // Fetch stand menu from server
        fetchMenu("food1");
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String b = (String) parent.getItemAtPosition(pos);
        fetchMenu(b);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // When a stand gets removed from the spinner this method is called
    }
}
