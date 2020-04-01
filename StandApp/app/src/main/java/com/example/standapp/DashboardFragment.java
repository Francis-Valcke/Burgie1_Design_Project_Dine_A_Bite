package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.json.CommonStand;
import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private ArrayList<CommonFood> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_dashboard_fragment, container, false);
        Button submitButton = view.findViewById(R.id.submit_menu_button);
        Button addButton = view.findViewById(R.id.add_menu_item_button);
        ListView menuList = view.findViewById(R.id.menu_list);

        // Getting the log in information from profile fragment
        final Bundle bundle = getArguments();
        String standName = "";
        String brandName = "";
        if (bundle != null && Utils.isLoggedIn(getContext(), bundle)) {
            standName = bundle.getString("standName");
            brandName = bundle.getString("brandName");
            Toast.makeText(getContext(), standName, Toast.LENGTH_SHORT).show();
        }

        final DashboardListViewAdapter adapter =
                new DashboardListViewAdapter(Objects.requireNonNull(getActivity()), items);
        menuList.setAdapter(adapter);

        // When logging into another stand account
        if (!items.isEmpty() && !items.get(0).getBrandName().equals(brandName)) {
            items.clear();
        }

        // Getting the stand menu from the server after logging in
        // when the stand has a menu saved on the server
        if (items.isEmpty() && bundle != null && Utils.isLoggedIn(getContext(), bundle)
                && Utils.isConnected(getContext())) {

            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
            String url = "http://cobol.idlab.ugent.be:8091/standmenu?standname=" + standName;
            url = url.replace(' ', '+');

            // Request menu from order manager on server
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        System.out.println(response.toString());
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        MenuItem[] parsedItems = mapper.readValue(response.toString(), MenuItem[].class);
                        Collections.addAll(items, parsedItems);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.v("Exception fetch menu:", e.toString());
                        Toast.makeText(getContext(), "Could not get menu from server!",
                                Toast.LENGTH_LONG).show();
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
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer" + " " + "eyJhbGciOiJIUzI1NiJ9.eyJzdW" +
                            "IiOiJmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJ" +
                            "pYXQiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK" +
                            "0OUlsbAPNyS9_vr-1QcOWnQ");
                    return headers;
                }
            };

            queue.add(jsonRequest);
        }

        final View addDialogLayout = inflater.inflate(R.layout.add_menu_item_dialog, null, false);
        final TextInputEditText nameInput = addDialogLayout.findViewById(R.id.menu_item_name);
        final TextInputEditText priceInput = addDialogLayout.findViewById(R.id.menu_item_price);
        final TextInputEditText stockInput = addDialogLayout.findViewById(R.id.menu_item_stock);
        final View finalView = view;

        // Adding a new menu item to the menu list of the stand
        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Objects.requireNonNull(this.getContext()))
                .setView(addDialogLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Objects.requireNonNull(nameInput.getText()).toString().isEmpty()
                                || Objects.requireNonNull(priceInput.getText()).toString().isEmpty()
                                || Objects.requireNonNull(stockInput.getText()).toString().isEmpty()) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(finalView.getContext())
                                    .setTitle("Invalid menu item")
                                    .setMessage("The menu item you tried to add is invalid, please try again.")
                                    .setNeutralButton("Ok", null);
                            alertDialog.show();
                        } else {
                            String name = Objects.requireNonNull(nameInput.getText()).toString();
                            BigDecimal price = new BigDecimal(Objects.requireNonNull(priceInput.getText()).toString());
                            int stock = Integer.parseInt(Objects.requireNonNull(stockInput.getText()).toString());
                            List<String> category = new ArrayList<>();
                            category.add("");
                            CommonFood item = new CommonFood(name, price, 150, stock, "", "", category);
                            items.add(item);
                            adapter.notifyDataSetChanged();
                            nameInput.setText("");
                            priceInput.setText("");
                            stockInput.setText("");
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

                    // create JSON string containing the information of the menu and the stand
                    long lat = 360L; // temporary
                    long lon = 360L; // temporary
                    CommonStand commonStand = new CommonStand(finalStandName, finalBrandName, lat, lon, items);
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = "";
                    try {
                        jsonString = mapper.writeValueAsString(commonStand);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    // Instantiate the RequestQueue
                    RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                    String url = ServerConfig.OM_ADDRESS + "/addStand";

                    // POST to server
                    final String finalJsonString = jsonString;
                    StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
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
                            headers.put("Authorization", ServerConfig.AUTHORIZATION_TOKEN);
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
}
