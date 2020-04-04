package com.example.attendeeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.attendeeapp.json.CommonFood;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * Handles the view for the global menu
 */
public class MenuFragmentGlobal extends MenuFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_global, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
                fetchMenu("", "");
                pullToRefresh.setRefreshing(false);
            }
        });

        // Fetch global menu from server
        fetchMenu("", "");
    }

    /**
     * Updates the global menu from the server response
     * Error are handled in the fetchMenu (superclass) function
     * @param response: the JSON response from the server
     * @param standName: the requested menu standName, "" is global
     * @throws JSONException
     */
    public void updateMenu(List<CommonFood> response, String standName) throws JSONException {
        // Renew the list
        menuItems.clear();

        menuItems.addAll(response);
//        Log.v("response", "Response: " + response.toString());

        menuAdapter.putList(menuItems);
        menuAdapter.notifyDataSetChanged();
    }

}
