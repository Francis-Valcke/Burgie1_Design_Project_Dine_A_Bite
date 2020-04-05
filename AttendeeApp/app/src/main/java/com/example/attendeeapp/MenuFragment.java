package com.example.attendeeapp;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.json.CommonFood;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;

/**
 * Abstract parent class of global and stand menuFragments
 * Contains the common variables and functions
 */
public abstract class MenuFragment extends Fragment {

    protected ArrayList<CommonFood> menuItems = new ArrayList<CommonFood>();
    protected MenuItemAdapter menuAdapter;
    protected SwipeRefreshLayout pullToRefresh;
    protected Toast mToast;

    /**
     * Updates the current global/stand menu with the updated version returned from the server
     * Error are handled in the fetchMenu function
     * @param response: the JSON response from the server
     */
    public void updateMenu(List<CommonFood> response) {
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
    protected void fetchMenu(final String standName, final String brandName){
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
        JsonArrayRequest jsonRequest = new JsonArrayRequest(req, url, null,
                                                            new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    ObjectMapper om = new ObjectMapper();
                    List<CommonFood> foodList=om.readValue(response.toString(), new TypeReference<List<CommonFood>>() {});

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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                // Hardcoded test menuItem to add when server is unavailable
                /*MenuItem item = new MenuItem("foodName", new BigDecimal(5.5), "brandName");
                menuItems.add(item);
                MenuItem item2 = new MenuItem("foody", new BigDecimal(6.11), "brand2");
                menuItems.add(item2);
                menuAdapter.putList(menuItems);
                menuAdapter.notifyDataSetChanged();*/

                // NoConnectionError = no network connection
                // other = server not reachable
                if (mToast != null) mToast.cancel();
                if (error instanceof NoConnectionError) {
                    mToast = Toast.makeText(getActivity(), "No network connection",
                                            Toast.LENGTH_LONG);

                } else {
                    mToast = Toast.makeText(getActivity(), "Server cannot be reached. No menu available.",
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


        String test1 = jsonRequest.getUrl();
        String test2 = jsonRequest.toString();
        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }
}
