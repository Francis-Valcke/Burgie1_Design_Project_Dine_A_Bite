package com.example.standapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.json.BetterResponseModel;
import com.example.standapp.json.CommonFood;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private String standName = "";
    private String brandName = "";
    private LoggedInUser user;

    // ID from the Event Channel
    private String subscriberId = null;

    private Context mContext;
    private Toast mToast;

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

        RevenueViewModel model = new ViewModelProvider(requireActivity()).get(RevenueViewModel.class);
        Observer<BigDecimal> revenueObserver = new Observer<BigDecimal>() {
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

                                // Keep verify button disabled when inputting same stand name
                                // else enable verify button
                                if (bundle != null
                                        && Objects.equals(bundle.getString("standName"), standName)) {
                                    verifyButton.setEnabled(false);
                                } else if (!standName.isEmpty() && !brandName.isEmpty()) {
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

                                // Keep verify button disabled when inputting same stand name
                                // else enable verify button
                                if (bundle != null
                                        && Objects.equals(bundle.getString("standName"), standName)) {
                                    verifyButton.setEnabled(false);
                                } else if (!standName.isEmpty() && !brandName.isEmpty()) {
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
                    if (bundle != null) bundle.putString("standName", null);
                    if (bundle != null) bundle.putString("brandName", null);
                    if (bundle != null) bundle.putString("subscriberId", null);

                    RevenueViewModel revenueViewModel = new ViewModelProvider(requireActivity())
                            .get(RevenueViewModel.class);
                    revenueViewModel.setRevenue(new BigDecimal(0));
                    revenueViewModel.resetPrices();

                    MenuViewModel menuViewModel = new ViewModelProvider(requireActivity())
                            .get(MenuViewModel.class);
                    menuViewModel.resetMenuList();

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
                                    if (response.get("details").equals("The stand does not exist and is free to be created")) {
                                        if (bundle != null) bundle.putBoolean("newStand", true);
                                    } else {
                                        if (bundle != null) bundle.putBoolean("newStand", false);
                                    }
                                    // The stand-brand has been verified by the server
                                    // - The user is the owner, or
                                    // - The stand-brand is new and the user can become the owner
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
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(getContext(), "Verify: " + error.toString(),
                                    Toast.LENGTH_LONG);
                            mToast.show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", user.getAuthorizationToken());
                            return headers;
                        }
                    };

                    queue.add(request);
                }
            }

        });

        logOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Unsubscribe to channels of Event Channel
                if (subscriberId != null) {
                    Utils.unsubscribeEC(subscriberId, standName, brandName, user);
                }

                // Clear Shared Preference file, this will reset the logged in user
                // - erase username
                // - erase user ID / token
                // - erase subscriber ID
                MainActivity parentActivity = (MainActivity) getActivity();
                if (parentActivity != null) {
                    parentActivity.clearCredentials(mContext);
                } else {
                    Log.d("LOG OUT ERROR", "Could not clear credentials!");
                }

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
        if (bundle != null) {
            if (mToast != null) mToast.cancel();
            Utils.isLoggedIn(mContext, bundle);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mToast != null) mToast.cancel();
    }

    /**
     * Handle situation after the stand name and brand given by the user is owned by the user
     * or is a new stand
     * This method will call method to fetch the previously saved/submitted menu from the server
     * (if it exists)
     * This method will call method to subscribe the stand to the Event Channel for incoming orders
     *
     * @param standName name of the stand given by user
     * @param brandName name of the brand given by user
     * @param bundle    Bundle to be shared between the fragments
     */
    private void handleVerify(final String standName, final String brandName, final Bundle bundle) {
        if (bundle != null) bundle.putString("standName", standName);
        if (bundle != null) bundle.putString("brandName", brandName);

        // Getting the stand menu from the server after logging in
        // when the stand has a menu saved on the server
        fetchMenu(mContext, standName, brandName, bundle);

        // Getting the revenue of the stand from the server after logging in
        // when the stand has a revenue saved on the server
        fetchRevenue(mContext, standName, brandName);

        // Subscribe to EC and retrieve subscriber ID
        subscribeEC(standName, brandName, bundle, new VolleyCallback() {

            @Override
            public void onSuccess(String result) {
                Log.d("ProfileFrag", "VolleyCallback on Success");

                // Store stand- and brand name persistently
                // Store received subscriber ID persistently
                // (wait for when response from server via callback)
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                            getString(R.string.shared_pref_file_key),
                            masterKeyAlias,
                            mContext,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("stand_name", standName);
                    editor.putString("brand_name", brandName);
                    editor.putString("subscriber_id", result);
                    editor.apply();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    /**
     * This method will subscribe the stand to the Event Channel
     * and set the subscriberId attribute
     *
     * @param standName name of the stand given by user
     * @param brandName name of the brand given by user
     * @param bundle    bundle to store the retrieved subscriber ID
     * @param callback  callback that handles response of volley request
     */
    private void subscribeEC(final String standName, final String brandName, final Bundle bundle,
                             final VolleyCallback callback) {

        // Step 1: Get subscriber ID
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        String url = ServerConfig.EC_ADDRESS + "/registerSubscriber";

        // GET request to server
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getContext(), "SubscriberId: " + response, Toast.LENGTH_SHORT);
                mToast.show();
                subscriberId = response;
                callback.onSuccess(response);
                if (bundle != null) bundle.putString("subscriberId", subscriberId);
                System.out.println("SubscriberID = " + subscriberId); // DEBUG

                // Step 2: Subscribe to stand and subscriberID channels
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
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", user.getAuthorizationToken());
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
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT);
                mToast.show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(request);
    }

    /**
     * This method will fetch the previously saved/submitted menu from the server
     * (if it exists) and save it to the bundle given
     *
     * @param standName name of the stand
     * @param brandName name of the brand
     * @param bundle    bundle to store the fetched menu
     */
    void fetchMenu(final Context context, String standName, String brandName, final Bundle bundle) {

        final LoggedInUser loggedInUser = LoginRepository.getInstance(new LoginDataSource())
                .getLoggedInUser();

        final ArrayList<CommonFood> items = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.OM_ADDRESS + "/standMenu?brandName=" + brandName
                + "&standName=" + standName;
        url = url.replace(' ', '+');

        // Request menu from order manager on server
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ObjectMapper mapper = new ObjectMapper();
                BetterResponseModel<ArrayList<CommonFood>> responseModel=null;
                try {
                   responseModel
                           = mapper.readValue(response.toString(),
                           new TypeReference<BetterResponseModel<ArrayList<CommonFood>>>() {});
                } catch (Exception e) {
                    Log.v("Exception fetch menu:", e.toString());
                    Toast.makeText(context, "Error parsing response while fetching menu",
                            Toast.LENGTH_LONG).show();
                }

                if (responseModel!=null) {
                    if (responseModel.isOk()) {
                        List<CommonFood> parsedItems = responseModel.getPayload();
                        items.addAll(parsedItems);

                        MenuViewModel menuViewModel = new ViewModelProvider(requireActivity())
                                .get(MenuViewModel.class);
                        menuViewModel.setMenuList(items);

                        RevenueViewModel revenueViewModel = new ViewModelProvider(requireActivity())
                                .get(RevenueViewModel.class);
                        for (CommonFood item : items) {
                            revenueViewModel.addPrice(item.getName(), item.getPrice());
                        }
                    }
                    else {
                        Toast.makeText(context, responseModel.getDetails(),
                                Toast.LENGTH_LONG).show();
                        MenuViewModel menuViewModel = new ViewModelProvider(requireActivity())
                                .get(MenuViewModel.class);
                        menuViewModel.setMenuList(items);
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof ServerError) {
                    Toast.makeText(context, "Server could not find menu of stand",
                            Toast.LENGTH_LONG).show();

                    // The given stand and brand names do not exist in the server database
                    //if (bundle != null) bundle.putBoolean("newStand", true);
                } else {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", loggedInUser.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(jsonRequest);
    }

    /**
     * This method will fetch the saved revenue of the logged in stand from the server
     *
     * @param context   context from which the method is called
     * @param standName stand name of logged in stand
     * @param brandName brand name of logged in stand
     */
    void fetchRevenue(final Context context, String standName, String brandName) {

        final LoggedInUser loggedInUser = LoginRepository.getInstance(new LoginDataSource())
                .getLoggedInUser();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.OM_ADDRESS + "/revenue?standName=" + standName +
                "&brandName=" + brandName;
        url = url.replace(' ', '+');

        JsonObjectRequest revenueRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ObjectMapper mapper = new ObjectMapper();
                BetterResponseModel<BigDecimal> responseModel = null;

                try {
                    responseModel = mapper.readValue(response.toString(),
                            new TypeReference<BetterResponseModel<BigDecimal>>() {
                    });
                } catch (JsonProcessingException e) {
                    Toast.makeText(context, "Error parsing revenue response",
                            Toast.LENGTH_LONG).show();
                }

                if (responseModel != null) {
                    if (responseModel.isOk()) {
                        BigDecimal revenue = responseModel.getPayload();
                        RevenueViewModel model = new ViewModelProvider(requireActivity())
                                .get(RevenueViewModel.class);
                        model.setRevenue(revenue);
                    } else {
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(context, responseModel.getDetails(), Toast.LENGTH_LONG);
                        mToast.show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //Toast.makeText(getContext(), "Revenue: " + error.toString(),Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", loggedInUser.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(revenueRequest);
    }
}
