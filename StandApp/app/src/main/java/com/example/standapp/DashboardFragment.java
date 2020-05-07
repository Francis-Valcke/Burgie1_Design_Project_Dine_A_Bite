package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.CommonStand;
import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment
        implements MenuItemFragment.OnMenuItemChangedListener {

    private Context mContext;
    private boolean isNewStand = false;
    private ArrayList<CommonFood> items = new ArrayList<>();
    private DashboardListViewAdapter adapter;

    // Stores the current stock of the menu items;
    // this way the stock send to the backend is calculated to be equal to the added stock
    private HashMap<String, Integer> addedStockMap = new HashMap<>();

    // Location data of stand
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;

    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
        mContext = context;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container,
                false);
        Button submitButton = view.findViewById(R.id.submit_menu_button);
        Button addButton = view.findViewById(R.id.add_menu_item_button);
        ListView menuList = view.findViewById(R.id.menu_list);

        final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        // Getting the log in information from profile fragment
        final Bundle bundle = getArguments();
        String standName = "";
        String brandName = "";
        if (bundle != null && Utils.isLoggedIn(getContext(), bundle)) {
            standName = bundle.getString("standName");
            brandName = bundle.getString("brandName");
            Toast.makeText(mContext, standName, Toast.LENGTH_SHORT).show();

            // Ask for permission to get location data and set lastLocation variable
            checkLocationPermission();

            items = (ArrayList<CommonFood>) bundle.getSerializable("items");
            isNewStand = bundle.getBoolean("newStand");
        }

        adapter = new DashboardListViewAdapter(Objects.requireNonNull(getActivity()), items,
                this);
        menuList.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Open dialog to fill in information for adding new menu item
                MenuItemFragment menuItemFragment = new MenuItemFragment();
                menuItemFragment.show(getChildFragmentManager().beginTransaction(),
                        "menu_item_dialog");
            }
        });

        // Submit and send the new or changed menu list to the backend
        final String finalBrandName = brandName;
        final String finalStandName = standName;
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bundle != null && Utils.isLoggedIn(mContext, bundle)
                        && Utils.isConnected(mContext)) {

                    HashMap<String, Integer> stock = new HashMap<>();

                    for (CommonFood item : items) {
                        item.setBrandName(finalBrandName);
                        item.setStandName(finalStandName);

                        if (item.getCategory().isEmpty()) {
                            item.addCategory("");
                        }

                        // Temporarily set stock to added stock,
                        // because that is what the backend expects,
                        // change back after sending to backend
                        // (ask Julien Van den Avenne)
                        stock.put(item.getName(), item.getStock());
                        if (addedStockMap.containsKey(item.getName())) {
                            item.setStock(Objects.requireNonNull(addedStockMap.get(item.getName())));
                        } else {
                            item.setStock(0);
                        }
                    }

                    // Set location data
                    checkLocationPermission();
                    double latitude = 360;
                    double longitude = 360;
                    if (lastLocation != null) {
                        latitude = lastLocation.getLatitude();
                        longitude = lastLocation.getLongitude();
                    }

                    // create JSON string containing the information of the menu and the stand
                    CommonStand commonStand = new CommonStand(finalStandName, finalBrandName,
                            latitude, longitude, items);
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = "";
                    try {
                        jsonString = mapper.writeValueAsString(commonStand);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    // Instantiate the RequestQueue
                    RequestQueue queue = Volley.newRequestQueue(mContext);
                    String url;
                    // Check if stand is new or not
                    if (items.isEmpty() || isNewStand) {
                        url = ServerConfig.OM_ADDRESS + "/addStand";
                        isNewStand = false;
                    } else {
                        url = ServerConfig.OM_ADDRESS + "/updateStand";
                    }

                    // POST to server
                    final String finalJsonString = jsonString;
                    StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Toast.makeText(getContext(), jsonObject.get("details").toString(),
                                        Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(mContext, "Error with JSON parsing: "
                                        + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public byte[] getBody() {
                            return finalJsonString.getBytes();
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/json";
                        }

                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", user.getAutorizationToken());
                            return headers;
                        }
                    };

                    // Add the request to the RequestQueue
                    queue.add(jsonRequest);
                    System.out.println(jsonString);

                    // Revert stock change
                    for (CommonFood item : items) {
                        item.setStock(Objects.requireNonNull(stock.get(item.getName())));
                    }
                    addedStockMap.clear();
                }
            }
        });

        Utils.isConnected(mContext);

        return view;
    }

    /**
     * Check if location permission is granted
     * It not: request the location permission
     * else if permission was granted, renew user location
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // Request the latest user location
            fusedLocationClient = LocationServices
                    .getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null){
                                lastLocation = task.getResult();
                            }
                        }
                    });
        }
    }

    /**
     * Handle the requested permissions,
     * here only the location permission is handled
     * @param requestCode: 1 = location permission was requested
     * @param permissions: the requested permission(s) names
     * @param grantResults: if the permission is granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // Create location request to fetch latest user location
                    fusedLocationClient = LocationServices
                            .getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
                    fusedLocationClient.getLastLocation()
                            .addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if (task.isSuccessful() && task.getResult() != null){
                                        lastLocation = task.getResult();
                                    }
                                }
                            });
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    @Override
    public void onMenuItemAdded(CommonFood item) {
        items.add(item);
        addedStockMap.put(item.getName(), item.getStock());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMenuItemChanged(CommonFood item, int addedStock, int position) {
        items.set(position, item);
        int curr = 0;
        if (addedStockMap.containsKey(item.getName())) {
            curr = Objects.requireNonNull(addedStockMap.get(item.getName()));
        }
        addedStockMap.put(item.getName(), curr + addedStock);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMenuItemDeleted(int position) {
        items.remove(position);
        adapter.notifyDataSetChanged();
    }
}
