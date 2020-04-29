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
import java.util.HashMap;
import java.util.Objects;

/**
 * Handles the view for the category menu's
 */
public class MenuFragmentCategory extends MenuFragment implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> categoryListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Create a spinner item for the different categories
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        categoryListAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.stand_spinner_item, new ArrayList<String>());
        categoryListAdapter.add("No categories available");
        spinner.setAdapter(categoryListAdapter);


        // Instantiates menu item list
        ListView lView = view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);


        // Setup swipe to refresh menu (e.g. no internet connection)
        // This will re-fetch the stand names from the server,
        // the spinner will call fetchMenu for the first stand item
        pullToRefresh = view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchMenu("", "");
                //categoryListAdapter.remove("No categories available");

                pullToRefresh.setRefreshing(true);
            }
        });

        // The spinner will call fetchMenu for the first stand item
        //fetchMenu("", "");
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected in the spinner, fetch the menu of the selected stand
        String standName = (String) parent.getItemAtPosition(pos);
        if (!standName.equals("No categories available")) {
            //fetchMenu(standName, "");//"".get(standName));
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // When a stand gets removed from the spinner this method is called
    }

}
