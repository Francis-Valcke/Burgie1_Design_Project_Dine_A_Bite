package com.example.attendeeapp;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.CommonFood;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract parent class of global and stand menuFragments
 * Contains the common variables and functions
 */
abstract class MenuFragment extends Fragment {

    ArrayList<CommonFood> menuItems = new ArrayList<>();
    MenuItemAdapter menuAdapter;
    SwipeRefreshLayout pullToRefresh;
    Toast mToast;

    protected LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    /**
     * Updates the current global/stand menu with the updated version returned from the server
     * Error are handled in the fetchMenu function
     * @param response: List of food items from the server
     */
    protected void updateMenu(List<CommonFood> response) {
        // Renew the list
        menuItems.clear();

        menuItems.addAll(response);
        //Log.v("response", "Response: " + response.toString());

        menuAdapter.putList(menuItems);
        menuAdapter.notifyDataSetChanged();
    }

    /**
     * Function to fetch the global or stand menu from the server in JSON
     * Handles no network connection or server not reachable
     * TODO: store menu in cache / fetch menu at splash screen /
     * TODO: do not update when menu has not changed
     * TODO: notify user when cart item no long available
     * @param standName: the name of the stand to request the menu of,
     *                "" if the global menu is required
     */
    void fetchMenu(final String standName, final String brandName){
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url = ServerConfig.OM_ADDRESS;
        int req = Request.Method.GET;
        if (standName.equals("")) {
            url = url + "/menu";
        } else {

            url = String.format("%1$s/standMenu?standName=%2$s&brandName=%3$s",
                    url,
                    standName.replace("&","%26"),
                    brandName.replace("&","%26"));
        }
        // Remove spaces from the url
        url = url.replace(' ', '+');

        // Request the global/stand menu in JSON from the order manager
        // Handle no network connection or server not reachable
        JsonObjectRequest jsonRequest = new JsonObjectRequest(req, url, null,
                response -> {
                    try {
                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<List<CommonFood>> responseModel= om.readValue(response.toString(), new TypeReference<BetterResponseModel<List<CommonFood>>>() {});

                        if(!responseModel.isOk()){
                            showToast(responseModel.getException().getMessage());
                            return;
                        }

                        List<CommonFood> foodList=responseModel.getPayload();

                        // For global menu, set stand names to ""
                        if (standName.equals("")) {
                            for (CommonFood food : foodList) {
                                food.setStandName("");
                            }
                        }

                        // Let fragments handle the response
                        updateMenu(foodList);
                    } catch (Exception e) { // Catch all exceptions TODO: only specific ones
                        Log.v("Exception fetchMenu", e.toString());
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(getActivity(), "A parsing error occurred when fetching the menu!",
                                Toast.LENGTH_LONG);
                        mToast.show();
                    }
                    // Refreshing is done
                    pullToRefresh.setRefreshing(false);
                }, error -> {

                    // Hardcoded test menuItem to add when server is unavailable
                    /*MenuItem item = new MenuItem("foodName", new BigDecimal(5.5), "brandName");
                    menuItems.add(item);
                    MenuItem item2 = new MenuItem("foody", new BigDecimal(6.11), "brand2");
                    menuItems.add(item2);
                    menuAdapter.putList(menuItems);
                    menuAdapter.notifyDataSetChanged();*/

                    // NoConnectionError = no network connection
                    // other = server not reachable
                    if (error instanceof NoConnectionError) {
                        showToast("No network connection");
                    } else {
                        showToast("Server cannot be reached. No menu available.");
                    }
                    // Refreshing is done
                    pullToRefresh.setRefreshing(false);
                }) { // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders() {
                Map<String, String>  headers  = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }

        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }


    private void showToast(String message){
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(getActivity(), message,
                Toast.LENGTH_LONG);
        mToast.show();
    }
}
