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

import com.example.attendeeapp.json.CommonFood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Handles the view for the category menu's.
 */
public class MenuFragmentCategory extends MenuFragment implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private ArrayAdapter<String> categoryListAdapter;
    // Map containing all items for a specific category, key = category, value = list of menu items
    private HashMap<String, ArrayList<CommonFood>> categoryItemMap = new HashMap<>();

    /**
     * Method to create the fragments View.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_category, container, false);
    }

    /**
     * Method to setup the fragments View.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Create a spinner item for the different categories
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        categoryListAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.stand_spinner_item, new ArrayList<>());
        categoryListAdapter.add("No categories available");
        spinner.setAdapter(categoryListAdapter);


        // Instantiates menu item list
        ListView lView = view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);


        // Setup swipe to refresh menu (e.g. no internet connection)
        // This will re-fetch all the menu items from the server
        // the spinner will initialize the first selected spinner category its menu items
        pullToRefresh = view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            fetchMenu("", "");
            pullToRefresh.setRefreshing(true);
        });

        // Fetch all menu items from the server (contain the categories)
        fetchMenu("", "");
        // The spinner will initialize category items for the first selected spinner category
    }

    /**
     * Method that sets the chosen category when an item is selected from the category spinner.
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected in the spinner, display the chosen category menu items
        String category = (String) parent.getItemAtPosition(pos);
        if (!category.equals("No categories available")) {
            menuAdapter.putList(categoryItemMap.get(category));
        } else {
            menuAdapter.putList(new ArrayList<>());
        }
        menuAdapter.notifyDataSetChanged();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // When a selected category gets removed from the spinner this method is called
    }


    /**
     * This method sorts the global menu items received from the server according to their category
     * and sets them to be used when a user selects a category.
     * It overrides the MenuFragment superclass updateMenu method for this category fragment.
     *
     * @param response List of food items received from the server.
     */
    protected void updateMenu(List<CommonFood> response) {
        // Renew the list
        menuItems.clear();
        categoryItemMap.clear();
        categoryListAdapter.clear();
        categoryListAdapter.add("No categories available");

        boolean others = false;
        for (CommonFood item : response) {
            List<String> categories = item.getCategory();
            for (String category : categories) {
                if (category.equals("")) {
                    others = true;
                    category = "OTHERS";
                }
                ArrayList<CommonFood> localList = categoryItemMap.get(category);
                if (localList != null) {
                    localList.add(item);
                } else {
                    localList = new ArrayList<>();
                    localList.add(item);
                    categoryItemMap.put(category, localList);
                    if (!category.equals("OTHERS")) categoryListAdapter.add(category);
                }
            }
        }

       menuItems.addAll(response);
        //Log.v("response", "Response: " + response.toString());
        // Add others as last category if necessary
        if (others) categoryListAdapter.add("OTHERS");
        if (categoryListAdapter.getCount() != 1) categoryListAdapter.remove("No categories available");

        // Refresh spinner
        spinner.setAdapter(categoryListAdapter);
    }

}
