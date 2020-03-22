package com.example.attendeeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Iterator;

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
                fetchMenu("");
                pullToRefresh.setRefreshing(false);
            }
        });

        // Fetch global menu from server
        fetchMenu("");
    }

    /**
     * Overloaded function of abstract MenuFragment superclass
     * Error are handled in the fetchMenu (superclass) function
     * @param response: the JSON response from the server
     * @throws JSONException
     */
    public void updateMenu(JSONObject response) throws JSONException {
        // Renew the list
        menuItems.clear();
        //Log.v("response", "Response: " + response.toString());
        for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
            String key = iter.next();
            String[] item_name = key.split("_");
            String foodName = item_name[0];
            String brandName = item_name[1];

            JSONArray jsonArray = response.getJSONArray(key);
            double price = jsonArray.getDouble(0);
            MenuItem item = new MenuItem(foodName, new BigDecimal(price), brandName);

            JSONArray cat_array = jsonArray.getJSONArray(1);
            for (int j = 0; j < cat_array.length(); j++) {
                item.addCategory((String) cat_array.get(j));
            }
            String description = jsonArray.getString(2);
            if (!description.equals("null")) item.setDescription(description);

            menuItems.add(item);
        }
        menuAdapter.putList(menuItems);
        menuAdapter.notifyDataSetChanged();
    }

}
