package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.CommonStand;
import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO fix sending and showing and storing stock
// TODO change stock based on incoming orders
// TODO set location data

public class DashboardFragment extends Fragment {

    private boolean newStand = false;
    private ArrayList<CommonFood> items = new ArrayList<>();

    // Location data of stand
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_dashboard_fragment, container, false);
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
            Toast.makeText(getContext(), standName, Toast.LENGTH_SHORT).show();

            // Ask for permission to get location data and set lastLocation variable
            checkLocationPermission();

            // Ignore warning
            items = (ArrayList<CommonFood>) bundle.getSerializable("items");
            newStand = bundle.getBoolean("newStand");
        }

        final DashboardListViewAdapter adapter =
                new DashboardListViewAdapter(Objects.requireNonNull(getActivity()), items);
        menuList.setAdapter(adapter);

        @SuppressLint("InflateParams")
        final View addDialogLayout = inflater.inflate(R.layout.add_menu_item_dialog, null, false);
        final TextInputEditText nameInput = addDialogLayout.findViewById(R.id.menu_item_name);
        final TextInputEditText priceInput = addDialogLayout.findViewById(R.id.menu_item_price);
        final TextInputEditText stockInput = addDialogLayout.findViewById(R.id.menu_item_stock);
        final TextInputEditText descriptionInput = addDialogLayout.findViewById(R.id.menu_item_description);
        final TextInputEditText prepTimeInput = addDialogLayout.findViewById(R.id.menu_item_prep_time);
        final View finalView = view;

        // Adding a new menu item to the menu list of the stand
        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Objects.requireNonNull(this.getContext()))
                .setView(addDialogLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Check if fields are filled in (except for description)
                        if (Objects.requireNonNull(nameInput.getText()).toString().isEmpty()
                                || Objects.requireNonNull(priceInput.getText()).toString().isEmpty()
                                || Objects.requireNonNull(stockInput.getText()).toString().isEmpty()
                                || Objects.requireNonNull(prepTimeInput.getText()).toString().isEmpty()) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(finalView.getContext())
                                    .setTitle("Invalid menu item")
                                    .setMessage("The menu item you tried to add is invalid, please try again.")
                                    .setNeutralButton("Ok", null);
                            alertDialog.show();
                        } else {
                            String name = Objects.requireNonNull(nameInput.getText()).toString();
                            BigDecimal price = new BigDecimal(priceInput.getText().toString());
                            int stock = Integer.parseInt(stockInput.getText().toString());
                            String description = Objects.requireNonNull(descriptionInput.getText()).toString();
                            int preparationTime = Integer.parseInt(prepTimeInput.getText().toString()) * 60;
                            List<String> category = new ArrayList<>();
                            category.add("");
                            CommonFood item = new CommonFood(name, price, preparationTime, stock, "", description, category);
                            items.add(item);
                            adapter.notifyDataSetChanged();
                            nameInput.setText("");
                            priceInput.setText("");
                            stockInput.setText("");
                            descriptionInput.setText("");
                            prepTimeInput.setText("");
                        }
                        ViewGroup parent = (ViewGroup) addDialogLayout.getParent();
                        if (parent != null) parent.removeView(addDialogLayout);
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ViewGroup parent = (ViewGroup) addDialogLayout.getParent();
                        if (parent != null) parent.removeView(addDialogLayout);
                    }
                });

        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // open dialog to fill in information
                dialog.show();
            }
        });

        // Submit and send the new or changed menu list to the backend
        final String finalBrandName = brandName;
        final String finalStandName = standName;
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bundle != null && Utils.isLoggedIn(getContext(), bundle)
                        && Utils.isConnected(getContext())) {

                    for (CommonFood item : items) {
                        item.setBrandName(finalBrandName);
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
                    CommonStand commonStand = new CommonStand(finalStandName, finalBrandName, latitude, longitude, items);
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = "";
                    try {
                        jsonString = mapper.writeValueAsString(commonStand);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    // Instantiate the RequestQueue
                    RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                    String url;
                    // Check if stand is new or not
                    if (items.isEmpty() || newStand) {
                        url = ServerConfig.OM_ADDRESS + "/addStand";
                        newStand = false;
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
                                Toast.makeText(getContext(), "Error with JSON parsing: "
                                        + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
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
                }
            }
        });

        Utils.isConnected(getContext());

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
                                public void onComplete(Task<Location> task) {
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
        }
    }
}
