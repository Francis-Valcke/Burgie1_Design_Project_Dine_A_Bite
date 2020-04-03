package com.example.attendeeapp;

import android.os.Bundle;
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
        //Log.v("response", "Response: " + response.toString());
//        for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
//            String key = iter.next();
//            String[] item_name = key.split("_");
//            String foodName = item_name[0];
//            String brandName = item_name[1];
//
//            // Create the menuItem with price, food and brandName
//            JSONArray jsonArray = response.getJSONArray(key);
//            double price = jsonArray.getDouble(0);
//            CommonFood item = new CommonFood(foodName, new BigDecimal(price), brandName);
//
//            // Add categories to the menuItem
//            if(!jsonArray.getString(1).equals("null")) {
//                JSONArray cat_array = jsonArray.getJSONArray(1);
//                for (int j = 0; j < cat_array.length(); j++) {
//                    item.addCategory((String) cat_array.get(j));
//                }
//            }
//            // Add the description, if provided
//            String description = jsonArray.getString(2);
//            if (!description.equals("null")) item.setDescription(description);
//
//            menuItems.add(item);
//        }
        menuAdapter.putList(menuItems);
        menuAdapter.notifyDataSetChanged();
    }

}
