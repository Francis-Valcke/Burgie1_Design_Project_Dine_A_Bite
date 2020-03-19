package com.example.attendeeapp;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstract parent class of global and stand menuFragments
 * Contains the common variables and functions
 */
public abstract class MenuFragment extends Fragment {

    protected ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
    protected MenuItemAdapter menuAdapter;
    protected SwipeRefreshLayout pullToRefresh;
    protected Toast mToast;


    /**
     * Function to fetch the global or stand menu from the server in JSON
     * Handles no network connection or server not reachable
     * TODO: store menu in cache / fetch menu at splash screen /
     * TODO: do not update when menu has not changed
     * TODO: notify user when cart item no long available
     * TODO: add standName to menuItem!
     * IMPORTANT:
     * TODO: change urls and change POST request to GET @server
     * @param standName: the name of the stand to request the menu of,
     *                "" if the global menu is required
     */
    protected void fetchMenu(String standName){
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "http://cobol.idlab.ugent.be:8092/";
        int req = Request.Method.GET;
        if(standName.equals("")){
            url = url + "menu";
        } else {
            //url = "http://localhost:8080/";
            url = url + "standmenu?standname=" + standName;
            req = Request.Method.POST;
        }

        // Request the global/stand menu in JSON from the stand manager
        // Handle no network connection or server not reachable
        JsonObjectRequest jsonRequest = new JsonObjectRequest(req, url, null,
                                                            new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Renew the list
                    menuItems.clear();
                    //Log.v("response", "Response: " + response.toString());
                    for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
                        String key = iter.next();
                        String price = response.getString(key);
                        menuItems.add(new MenuItem(key, new BigDecimal(Double.valueOf(price))));
                    }
                    menuAdapter.putList(menuItems);
                    menuAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // NoConnectionError = no network connection
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
                headers.put("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi" +
                        "JmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYX" +
                        "QiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0O" +
                        "UlsbAPNyS9_vr-1QcOWnQ");
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }
}
