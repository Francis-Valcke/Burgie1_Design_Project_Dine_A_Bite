package com.example.attendeeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.json.CommonFood;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;

/**
 * Handles the view for the stand menu's
 */
public class MenuFragmentStand extends MenuFragment implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> standListAdapter;
    // List of stand and brands: key = standName, value = brandName
    private HashMap<String, String> standList = new HashMap<String, String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_stand, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Create a spinner item for the different stands
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        standListAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.stand_spinner_item, new ArrayList<String>());
        spinner.setAdapter(standListAdapter);

        // Instantiates menu item list
        ListView lView = (ListView) view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);


        // Setup swipe to refresh menu (e.g. no internet connection)
        // This will refetch the stand names from the server,
        // the spinner will call fetchMenu for the first stand item
        pullToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchStandNames();
                pullToRefresh.setRefreshing(false);
            }
        });

        // Fetch stand names from the server
        fetchStandNames();
        // The spinner will call fetchMenu for the first stand item
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected in the spinner, fetch the menu of the selected stand
        String standName = (String) parent.getItemAtPosition(pos);
        fetchMenu(standName);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // When a stand gets removed from the spinner this method is called
    }

    /**
     * Function to fetch the stand names from the server
     */
    public void fetchStandNames() {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = ServerConfig.OM_ADDRESS + "/stands";

        // Request the stand names in JSON from the order manager
        // Handle no network connection or server not reachable
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            standListAdapter.clear();
                            for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
                                String standName = iter.next();
                                String brandName = response.getString(standName);
                                standList.put(standName, brandName);

                                // Add stand to the spinner list
                                standListAdapter.add(standName);
                            }

                        } catch (Exception e) { // Catch all exceptions TODO: only specific ones
                            Log.v("Exception fetchMenu", e.toString());
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(getActivity(), "An error occurred when fetching the menu!",
                                    Toast.LENGTH_LONG);
                            mToast.show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // NoConnectionError = no network connection0
                // other = server not reachable
                if (mToast != null) mToast.cancel();
                if (error instanceof NoConnectionError) {
                    mToast = Toast.makeText(getActivity(), "No network connection",
                            Toast.LENGTH_LONG);

                } else {
                    mToast = Toast.makeText(getActivity(), "Server cannot be reached. Try again later.",
                            Toast.LENGTH_LONG);
                }
                mToast.show();
            }
        }) { // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders()  throws AuthFailureError {
                Map<String, String>  headers  = new HashMap<String, String>();
                headers.put("Authorization", AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Updates a specific stand menu from the server response
     * Error are handled in the fetchMenu (superclass) function
     * @param response: the JSON response from the server
     * @param standName: the requested menu standName, "" is global
     * @throws JSONException
     */
    public void updateMenu(JSONObject response, String standName) throws JSONException {
        // Renew the list
        menuItems.clear();
        //Log.v("response", "Response: " + response.toString());
        for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
            String foodName = iter.next();

            // Create the menuItem with price, food and brandName
            JSONArray jsonArray = response.getJSONArray(foodName);
            String brandName = jsonArray.getString(0);
            double price = jsonArray.getDouble(1);
            CommonFood item = new CommonFood(foodName, new BigDecimal(price), brandName);
            item.setStandName(standName);

            // Add categories to the menuItem
            JSONArray cat_array = jsonArray.getJSONArray(2);
            for (int j = 0; j < cat_array.length(); j++) {
                item.addCategory((String) cat_array.get(j));
            }

            // Add the description, if provided
            String description = jsonArray.getString(3);
            if (!description.equals("null")) item.setDescription(description);

            menuItems.add(item);
        }
        menuAdapter.putList(menuItems);
        menuAdapter.notifyDataSetChanged();
    }
}
