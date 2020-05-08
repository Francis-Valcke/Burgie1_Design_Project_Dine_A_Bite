package com.example.standapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private String standName = "";
    private String brandName = "";
    private LoggedInUser user;

    // ID from the Event Channel
    private String subscriberId;

    // Used for subscriber ID, for when the user presses the verify button,
    // it does not ask for another subscriber ID from the Event Channel
    private String oldStand;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView usernameTextView = view.findViewById(R.id.username);
        final TextView standNameTextView = view.findViewById(R.id.stand_name);
        final TextView brandNameTextView = view.findViewById(R.id.brand_name);
        final TextView revenueTextView = view.findViewById(R.id.revenue_amount);
        Button editStandNameButton = view.findViewById(R.id.edit_stand_name_button);
        Button editBrandNameButton = view.findViewById(R.id.edit_brand_name_button);
        final Button verifyButton = view.findViewById(R.id.button_verify);
        Button logOutButton = view.findViewById(R.id.button_log_out);

        user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();
        usernameTextView.setText(user.getDisplayName());

        final Bundle bundle = getArguments();
        if (bundle != null) standNameTextView.setText(bundle.getString("standName"));
        if (bundle != null) brandNameTextView.setText(bundle.getString("brandName"));
        final RevenueViewModel model = new ViewModelProvider(requireActivity()).get(RevenueViewModel.class);
        final Observer<BigDecimal> revenueObserver = new Observer<BigDecimal>() {
            @Override
            public void onChanged(@Nullable final BigDecimal bigDecimal) {
                assert bigDecimal != null;
                String revenueString = "€ " + bigDecimal.toString();
                revenueTextView.setText(revenueString);
            }
        };
        model.getRevenue().observe(getViewLifecycleOwner(), revenueObserver);

        verifyButton.setEnabled(false);

        // Dialog for editing stand name
        final View inputStandNameLayout = inflater.inflate(R.layout.edit_name_dialog,
                container, false);
        final TextInputEditText editTextStandName
                = inputStandNameLayout.findViewById(R.id.edit_text_name);
        final MaterialAlertDialogBuilder dialogStandName =
                new MaterialAlertDialogBuilder(mContext)
                .setView(inputStandNameLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        standNameTextView.setText(editTextStandName.getText());
                        standName = Objects.requireNonNull(editTextStandName.getText()).toString();
                        editTextStandName.setText("");
                        ViewGroup parent = (ViewGroup) inputStandNameLayout.getParent();
                        parent.removeView(inputStandNameLayout);

                        if (!standName.isEmpty() && !brandName.isEmpty()){
                            verifyButton.setEnabled(true);
                        }
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editTextStandName.setText("");
                        ViewGroup parent = (ViewGroup) inputStandNameLayout.getParent();
                        if (parent != null) parent.removeView(inputStandNameLayout);
                    }
                });

        editStandNameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogStandName.show();
            }

        });

        // Dialog for editing brand name
        final View inputBrandNameLayout = inflater.inflate(R.layout.edit_name_dialog,
                container, false);
        final TextInputEditText editTextBrandName
                = inputBrandNameLayout.findViewById(R.id.edit_text_name);
        final MaterialAlertDialogBuilder dialogBrandName =
                new MaterialAlertDialogBuilder(mContext)
                .setView(inputBrandNameLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        brandNameTextView.setText(editTextBrandName.getText());
                        brandName = Objects.requireNonNull(editTextBrandName.getText()).toString();
                        editTextBrandName.setText("");
                        ViewGroup parent = (ViewGroup) inputBrandNameLayout.getParent();
                        parent.removeView(inputBrandNameLayout);

                        if (!standName.isEmpty() && !brandName.isEmpty()){
                            verifyButton.setEnabled(true);
                        }
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editTextBrandName.setText("");
                        ViewGroup parent = (ViewGroup) inputBrandNameLayout.getParent();
                        if (parent != null) parent.removeView(inputBrandNameLayout);
                    }
                });

        editBrandNameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBrandName.show();
            }

        });

        // Verify stand name and brand name with backend#order_manager
        verifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!standName.isEmpty() && !brandName.isEmpty() && Utils.isConnected(getContext())) {

                    verifyButton.setEnabled(false);

                    // Delete data from previous logged in stand
                    if (bundle != null) bundle.putSerializable("items", new ArrayList<CommonFood>());
                    if (bundle != null) bundle.putString("standName", null);
                    if (bundle != null) bundle.putString("brandName", null);
                    if (bundle != null) bundle.putString("subscriberId", null);

                    // POST request to /verify
                    RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                    String url = ServerConfig.OM_ADDRESS + "/verify?brandName=" + brandName
                            + "&standName=" + standName;
                    url = url.replace(' ', '+');

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                            null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            try {
                                Toast.makeText(getContext(),
                                        response.get("details").toString(),
                                        Toast.LENGTH_LONG).show();
                                if (response.get("status").equals("OK")) {
                                    handleVerify(standName, brandName, bundle);
                                } else {
                                    standName = null;
                                    brandName = null;
                                    standNameTextView.setText("");
                                    brandNameTextView.setText("");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getContext(), "Verify: " + error.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", user.getAutorizationToken());
                            return headers;
                        }
                    };

                    queue.add(request);

                    // Get revenue from server
                    String newUrl = ServerConfig.OM_ADDRESS + "/revenue?standName=" + standName + "&brandName="
                            + brandName;
                    newUrl = newUrl.replace(' ', '+');
                    JsonObjectRequest revenueRequest = new JsonObjectRequest(Request.Method.GET, newUrl
                            ,null, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            BigDecimal revenue = BigDecimal.valueOf((response));
                            model.setRevenue(revenue);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getContext(), "Revenue: " + error.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", user.getAutorizationToken());
                            return headers;
                        }
                    };

                    queue.add(revenueRequest);


                } else {
                    Toast.makeText(getContext(), "Input fields cannot be empty",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });

        logOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Delete data when data is persistent (in database)
                // TODO

                LoginRepository.getInstance(new LoginDataSource()).logout();

                // Start MainActivity again after finishing current MainActivity
                // Finishing the current MainActivity will delete all data in memory
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }

        });

        // Checks
        Utils.isConnected(mContext);
        if (bundle != null) Utils.isLoggedIn(mContext, bundle);

        return view;
    }

    /**
     * Handle situation after the stand name and brand given by the user is owned by the user
     * or is a new stand
     * This method will fetch the previously saved/submitted menu from the server (if it exists)
     * This method will subscribe the stand to the Event Channel for incoming orders
     * @param standName name of the stand given by user
     * @param brandName name of the brand given by user
     * @param bundle Bundle to be shared between the fragments
     */
    private void handleVerify(String standName, String brandName, final Bundle bundle) {
        if (bundle != null) bundle.putString("standName", standName);
        if (bundle != null) bundle.putString("brandName", brandName);

        // Getting the stand menu from the server after logging in
        // when the stand has a menu saved on the server
        final ArrayList<CommonFood> items = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        String url = ServerConfig.OM_ADDRESS + "/standMenu?brandName=" + brandName
                + "&standName=" + standName;
        url = url.replace(' ', '+');

        if (bundle != null) bundle.putBoolean("newStand", false);

        // Request menu from order manager on server
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    RevenueViewModel model = new ViewModelProvider(requireActivity()).get(RevenueViewModel.class);
                    System.out.println(response.toString());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    CommonFood[] parsedItems = mapper.readValue(response.toString(), CommonFood[].class);
                    Collections.addAll(items, parsedItems);
                    if (bundle != null) bundle.putSerializable("items", items);
                    for (CommonFood item : items) {
                        model.addPrice(item.getName(), item.getPrice());
                    }
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
                if (error instanceof ServerError) {
                    // TODO server should handle this exception and send a response
                    Toast.makeText(getContext(), "Server could not find menu of stand",
                            Toast.LENGTH_LONG).show();
                    if (bundle != null) bundle.putBoolean("newStand", true);
                } else {
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAutorizationToken());
                return headers;
            }
        };

        queue.add(jsonRequest);

        // Subscribe to EC
        if (subscriberId == null || subscriberId.equals("") || !oldStand.equals(standName)) {
            subscribeEC(standName, brandName, bundle);
            oldStand = standName;
        }

    }

    /**
     * This function will subscribe the stand to the Event Channel
     * and set the subscriberId attribute
     * @param standName name of the stand given by user
     * @param brandName name of the brand given by user
     */
    private void subscribeEC(final String standName, final String brandName, final Bundle bundle) {

        // Step 1: Get subscriber ID
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber";

        // GET request to server
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getContext(), "SubscriberId: " + response, Toast.LENGTH_SHORT)
                        .show();
                subscriberId = response;
                if (bundle != null) bundle.putString("subscriberId", subscriberId);

                // Step 2: Subscribe to stand and subscriberID channels
                System.out.println("SubscriberID = " + subscriberId);
                String url2 = ServerConfig.EC_ADDRESS + "/registerSubscriber/toChannel?type=s_"
                        + standName + "_" + brandName + "&id=" + subscriberId;
                url2 = url2.replace(' ', '+');

                // GET request to server
                final String finalUrl = url2;
                StringRequest request2 = new StringRequest(Request.Method.GET, url2,
                        new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response on GET request to " + finalUrl + ": " + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", user.getAutorizationToken());
                        return headers;
                    }
                };
                RequestQueue queue2 = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                queue2.add(request2);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAutorizationToken());
                return headers;
            }
        };

        queue.add(request);
    }
}
