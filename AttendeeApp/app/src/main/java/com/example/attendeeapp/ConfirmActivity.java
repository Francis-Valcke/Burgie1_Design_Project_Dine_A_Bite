package com.example.attendeeapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.json.CommonFood;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;

/**
 * Activity that handles the confirmation/choosing of the (recommended) stand of the placed order
 */
public class ConfirmActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> standListAdapter;
    private Location lastLocation;
    private Multimap<String, String> brandStandMap = ArrayListMultimap.create();
    private ArrayList<CommonFood> ordered;
    private int cartCount;
    private BigDecimal totalPrice;
    private List<Recommendation> recommendations = null;
    private CommonOrder orderReceived = null;
    private String chosenStand = null;
    private Toast mToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Ignore warning
        ordered = (ArrayList<CommonFood>) getIntent().getSerializableExtra("order");
        cartCount = getIntent().getIntExtra("cartCount", 0);
        totalPrice = (BigDecimal) getIntent().getSerializableExtra("totalPrice");
        lastLocation = getIntent().getParcelableExtra("location");
        requestOrderRecommend();
        fetchStandNames();

        // Make distance and time of recommendation invisible until recommendation comes available
        TextView distanceText = findViewById(R.id.recommend_distance_text);
        distanceText.setVisibility(View.INVISIBLE);
        TextView distance = findViewById(R.id.recommend_distance);
        distance.setVisibility(View.INVISIBLE);

        /*TextView remainingTimeText = findViewById(R.id.recommend_time_text);
        remainingTimeText.setVisibility(View.INVISIBLE);
        TextView remainingTime = findViewById(R.id.recommend_time);
        remainingTime.setVisibility(View.INVISIBLE);*/

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Create a spinner item for the different stands
        Spinner spinner = findViewById(R.id.stand_recommended_spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        standListAdapter = new ArrayAdapter<>(this,
                R.layout.stand_spinner_item, new ArrayList<String>());
        spinner.setAdapter(standListAdapter);

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);


        // Handle choose own stand and choose recommended stand buttons
        Button chooseStandButton = findViewById(R.id.button_confirm_stand);
        chooseStandButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(chosenStand != null) {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(ConfirmActivity.this, "This function is currently not supported yet",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                    /*boolean noRecommend = true;
                    if(recommendations != null) {
                        if (recommendations.size() > 0) {
                            noRecommend = false;
                            // Alert user if he not better like the recommended stand
                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmActivity.this);

                            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked Continue button
                                    dialog.cancel();

                                    // Continue to order overview with chosen stand
                                    Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                                    intent.putExtra("order", orderReceived);
                                    intent.putExtra("stand", chosenStand);
                                    intent.putExtra("brand", ordered.get(0).getBrandName());
                                    startActivity(intent);

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.cancel();
                                }
                            });

                            builder.setMessage("You have a recommendation available." +
                                    "\nAre you sure you want to choose your own stand?")
                                    .setTitle("Continue with chosen stand");
                            AlertDialog dialog = builder.create();
                            dialog.show();

                        }
                    }
                    if(noRecommend) {
                        if(orderReceived != null) {

                            // Continue to order overview with chosen stand
                            Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                            // TODO: handle no order received back from server
                            intent.putExtra("order", orderReceived);
                            intent.putExtra("stand", chosenStand);
                            intent.putExtra("brand", ordered.get(0).getBrandName());
                            startActivity(intent);

                        } else {
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(ConfirmActivity.this,
                                    "Your order could not be received, you cannot continue",
                                    Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                    }*/


                } else {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(ConfirmActivity.this, "You have no stand selected",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });

        Button chooseRecommendButton = findViewById(R.id.button_confirm_recommend);
        chooseRecommendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean noRecommend = true;
                if(recommendations != null) {
                    if (recommendations.size() > 0) {
                        noRecommend = false;

                        // Continue to order overview with recommended stand
                        Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                        intent.putExtra("order", orderReceived);
                        intent.putExtra("stand", recommendations.get(0).getStandName());
                        intent.putExtra("brand", recommendations.get(0).getBrandName());
                        startActivity(intent);
                    }
                }
                if(noRecommend) {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(ConfirmActivity.this, "No recommendation available.",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }

            }
        });

    }

    /**
     * Sends order of user to the server in JSON to request a recommendation
     * Send a JSON object with ordered items and user location
     * Format: CommonOrder converted to JSON
     * Location is (360, 360) when user location is unknown
     */
    private void requestOrderRecommend() {

        //360 is value for location unknown
        double latitude = 360;
        double longitude = 360;
        if(lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        // Make JSON Object with ordered items and location
        CommonOrder orderSent = new CommonOrder(ordered, ordered.get(0).getStandName(), ordered.get(0).getBrandName(), latitude, longitude);
        JSONObject jsonOrder = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonOrderString = mapper.writeValueAsString(orderSent);
            jsonOrder = new JSONObject(jsonOrderString);

        } catch (JsonProcessingException | JSONException e) {
            Log.v("JsonException in cart", e.toString());
        }

        // Remove unnecessary initial values, this will be set by server
        jsonOrder.remove("id");
        jsonOrder.remove("startTime");
        jsonOrder.remove("expectedTime");
        jsonOrder.remove("standId");

        // TODO: to remove the following, when server can handle updated CommonOrderItem
        try {
            JSONArray array = jsonOrder.getJSONArray("orderItems");
            for(int i =0 ; i<array.length(); i++){
                JSONObject item = (JSONObject) array.get(i);
                item.remove("price");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerConfig.OM_ADDRESS + "/placeOrder";


        // Request recommendation from server for sent order (both in JSON)
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonOrder,
                                                            new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ObjectMapper mapper= new ObjectMapper();
                        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        try {
                            recommendations= mapper.readValue(response.get("recommendations").toString(), new TypeReference<List<Recommendation>>() {});
                            orderReceived= mapper.readValue(response.get("order").toString(), CommonOrder.class);
                            orderReceived.setTotalPrice(totalPrice);
                            orderReceived.setPrices(ordered);
                            orderReceived.setTotalCount(cartCount);
                            // TODO: add ALL menuItem information to the orderItems!

                            showRecommendation();

                        } catch (JsonProcessingException | JSONException e) {
                            Log.v("JSON exception","JSON exception in confirmActivity");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(ConfirmActivity.this, "Recommendation could not be fetched.",
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
        }) { // Add JSON headers
            @Override
            public @NonNull Map<String, String> getHeaders() {
                Map<String, String>  headers  = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Function to fetch the stand names from the server
     */
    public void fetchStandNames() {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
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
                                brandStandMap.put(brandName, standName);
                            }
                            // Only stands of the brand selected in the order can be shown
                            for (String s : brandStandMap.get(ordered.get(0).getBrandName())) {
                                standListAdapter.add(s);
                            }

                        } catch (Exception e) { // Catch all exceptions TODO: only specific ones
                            Log.v("Exception fetchMenu", e.toString());
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(ConfirmActivity.this, "An error occurred when fetching the stands!",
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
                    mToast = Toast.makeText(ConfirmActivity.this, "No network connection",
                            Toast.LENGTH_LONG);
                    mToast.show();
                } else {
                    mToast = Toast.makeText(ConfirmActivity.this, "Server cannot be reached. No stands available.",
                            Toast.LENGTH_LONG);
                    mToast.show();
                }
            }
        }) { // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders() {
                Map<String, String>  headers  = new HashMap<>();
                headers.put("Authorization", AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Function that updates the textViews to show the received recommendation info
     */
    public void showRecommendation() {
        if(recommendations != null) {
            if(recommendations.size() > 0) {
                TextView recommend = findViewById(R.id.stand_recommend_text);
                recommend.setText(recommendations.get(0).getStandName());
                recommend.setTypeface(recommend.getTypeface(), Typeface.BOLD);

                TextView distanceText = findViewById(R.id.recommend_distance_text);
                distanceText.setVisibility(View.VISIBLE);

                TextView distance = findViewById(R.id.recommend_distance);
                distance.setText(Math.round(recommendations.get(0).getDistance()) + " meter");
                distance.setVisibility(View.VISIBLE);

                /*TextView remainingTimeText = findViewById(R.id.recommend_time_text);
                remainingTimeText.setVisibility(View.VISIBLE);

                TextView remainingTime = findViewById(R.id.recommend_time);
                remainingTime.setText(recommendations.get(0).getTimeEstimate()/60 + " minute(s)");
                remainingTime.setVisibility(View.VISIBLE);*/
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon
                // on the main android toolbar.
                onBackPressed();
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_action:
                // User chooses the "Account" item
                // TODO make account activity
                return true;
            case R.id.settings_action:
                // User chooses the "Settings" item
                // TODO make settings activity
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An item was selected in the spinner
        chosenStand = (String) adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
