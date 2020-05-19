package com.example.attendeeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Handles the view for the global menu.
 */
public class MenuFragmentGlobal extends MenuFragment {

    /**
     * Method to create the fragments View.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_global, container, false);
    }

    /**
     * Method to setup the fragments View.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Instantiates menu item list
        ListView lView = view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);

        // Setup swipe to refresh menu (e.g. no internet connection)
        pullToRefresh = view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            fetchMenu("", "");
            pullToRefresh.setRefreshing(true);
        });

        // Fetch global menu from server
        fetchMenu("", "");
    }

}
