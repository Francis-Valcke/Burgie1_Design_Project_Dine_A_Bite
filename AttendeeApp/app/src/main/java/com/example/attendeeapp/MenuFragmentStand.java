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

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.json.BetterResponseModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles the view for the stand menu's
 */
public class MenuFragmentStand extends MenuFragment implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private ArrayAdapter<String> standListAdapter;
    // List of brands belonging to a stand (in sequence!)
    private ArrayList<String> brandList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_stand, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Create a spinner item for the different stands
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        standListAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.stand_spinner_item, new ArrayList<>());
        standListAdapter.add("No stands available");
        spinner.setAdapter(standListAdapter);

        // Instantiates menu item list
        ListView lView = view.findViewById(R.id.menu_list);
        menuAdapter = new MenuItemAdapter(menuItems, getActivity());
        menuAdapter.setCartChangeListener((OnCartChangeListener) getActivity());
        lView.setAdapter(menuAdapter);


        // Setup swipe to refresh menu (e.g. no internet connection)
        // This will re-fetch the stand names from the server,
        // the spinner will call fetchMenu for the first stand item
        pullToRefresh = view.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            fetchStandNames();
            pullToRefresh.setRefreshing(true);
        });

        // Fetch stand names from the server
        fetchStandNames();
        // The spinner will call fetchMenu for the first stand item
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected in the spinner, fetch the menu of the selected stand
        String standName = (String) parent.getItemAtPosition(pos);
        if (!standName.equals("No stands available")) {
            fetchMenu(standName, brandList.get(pos));
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // When a stand gets removed from the spinner this method is called
    }

    /**
     * Function to fetch the stand names from the server
     */
    private void fetchStandNames() {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url = ServerConfig.OM_ADDRESS + "/stands";

        // Request the stand names in JSON from the order manager
        // Handle no network connection or server not reachable
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {

                    try {
                        standListAdapter.clear();
                        brandList.clear();
                        standListAdapter.add("No stands available");
                        ObjectMapper om=new ObjectMapper();
                        BetterResponseModel<Map<String, String>> responseModel= om.readValue(response.toString(), new TypeReference<BetterResponseModel<Map<String, String>>>() {});
                        if(!responseModel.isOk()){
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(getActivity(), responseModel.getException().getMessage(),
                                    Toast.LENGTH_LONG);
                            mToast.show();
                            return;
                        }


                        for (String standName : responseModel.getPayload().keySet()) {
                            String brandName = responseModel.getPayload().get(standName);
                            brandList.add(brandName);

                            // Add stand to the spinner list
                            standListAdapter.add(standName);
                        }

                        if (standListAdapter.getCount() != 1) standListAdapter.remove("No stands available");

                        // Refresh spinner
                        spinner.setAdapter(standListAdapter);

                    } catch (Exception e) { // Catch all exceptions TODO: only specific ones
                        Log.v("Exception fetchMenu", e.toString());
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(getActivity(), "A parsing error occurred when fetching the stands!",
                                Toast.LENGTH_LONG);
                        mToast.show();
                    }
                }, error -> {
                    // NoConnectionError = no network connection0
                    // other = server not reachable
                    if (mToast != null) mToast.cancel();
                    if (error instanceof NoConnectionError) {
                        mToast = Toast.makeText(getActivity(), "No network connection",
                                Toast.LENGTH_LONG);
                        mToast.show();
                    } else {
                        mToast = Toast.makeText(getActivity(), "Server cannot be reached. No stands available.",
                                Toast.LENGTH_LONG);
                        mToast.show();
                    }
                    // Refreshing is done
                    pullToRefresh.setRefreshing(false);
                }) { // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders()  {
                Map<String, String>  headers  = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

}
