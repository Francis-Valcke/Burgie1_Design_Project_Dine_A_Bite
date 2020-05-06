package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.CommonFood;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.internal.service.Common;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Activity that handles the confirmation/choosing of the (recommended) stand of the placed order
 */
public class ConfirmActivity extends ToolbarActivity implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> standListAdapter;
    private Location lastLocation;
    private ArrayList<CommonFood> ordered;
    private int cartCount;
    private BigDecimal totalPrice;
    private List<Recommendation> recommendations = null;
    private CommonOrder orderReceived = null;
    private int chosenRecommend = -1; // index in the recommendation list of the chosen recommend
    private String specificStand;
    private String specificBrand;
    private Recommendation specificRecommendation = null;
    private Toast mToast = null;
    private CommonOrder.recommendType recommendType = null;

    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

        // Ignore warning
        ordered = (ArrayList<CommonFood>) getIntent().getSerializableExtra("order");
        cartCount = getIntent().getIntExtra("cartCount", 0);
        totalPrice = (BigDecimal) getIntent().getSerializableExtra("totalPrice");
        lastLocation = getIntent().getParcelableExtra("location");
        recommendType = (CommonOrder.recommendType) getIntent().getSerializableExtra("recType");
        requestOrderRecommend();

        // Check if the user wants to order from a specific stand (all ordered items are from the same stand/brand)
        specificStand = ordered.get(0).getStandName();
        specificBrand = ordered.get(0).getBrandName();
        for (CommonFood i : ordered.subList(1, ordered.size())) {
            if (! (specificStand.equals(i.getStandName()) && specificBrand.equals(i.getBrandName())) ) {
                specificStand = "";
                specificBrand = "";
                break;
            }
        }

        // Make distance and time of recommendation invisible until recommendation comes available
        TextView distanceText = findViewById(R.id.recommend_distance_text);
        distanceText.setVisibility(View.GONE);
        TextView distance = findViewById(R.id.recommend_distance);
        distance.setVisibility(View.GONE);

        TextView remainingTimeText = findViewById(R.id.recommend_time_text);
        remainingTimeText.setVisibility(View.GONE);
        TextView remainingTime = findViewById(R.id.recommend_time);
        remainingTime.setVisibility(View.GONE);


        // Create a spinner item for the different stands
        Spinner spinner = findViewById(R.id.stand_recommended_spinner);
        spinner.setOnItemSelectedListener(this);

        // Initiate the spinner item adapter
        standListAdapter = new ArrayAdapter<>(this,
                R.layout.stand_spinner_item, new ArrayList<String>());
        spinner.setAdapter(standListAdapter);
        if (!specificStand.equals("")) {
            standListAdapter.add(specificStand + " (Your stand)");
        } else {
            standListAdapter.add("No stands available");
        }

        Button chooseRecommendButton = findViewById(R.id.button_confirm_stand);
        chooseRecommendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean noRecommend = true;
                if (recommendations != null && orderReceived != null) {
                    // An order could be received to the server
                    if (recommendations.size() > 0 && (
                            (specificRecommendation != null || chosenRecommend != -1)
                                    || specificStand.equals("")) ) {
                        noRecommend = false;

                        // Continue to order overview with recommended stand
                        Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                        intent.putExtra("order", orderReceived);
                        intent.putExtra("stand", recommendations.get(chosenRecommend).getStandName());
                        intent.putExtra("brand", recommendations.get(chosenRecommend).getBrandName());
                        startActivity(intent);
                    } else if (recommendations.size() > 0) {
                        // specificRecommendation is not part of the returned recommendations
                        noRecommend = false;

                        // Alert user if he not better like the recommended stand
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmActivity.this);

                        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked Continue button
                                dialog.cancel();

                                // TODO: update expected timings when
                                //  order from specific stand without recommendation is made !! important
                                //  (need timing for the order from server)
                                // Continue to order overview with chosen stand
                                Intent intent = new Intent(ConfirmActivity.this, OrderActivity.class);
                                intent.putExtra("order", orderReceived);
                                intent.putExtra("stand", specificStand);
                                intent.putExtra("brand", specificBrand);
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
                if (noRecommend) {
                    String text = "No stands available";
                    if (!specificStand.equals(""))
                        text = "Your order could not be received, you cannot continue";
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(ConfirmActivity.this, text, Toast.LENGTH_SHORT);
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
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        // Make JSON Object with ordered items and location
        CommonOrder orderSent = new CommonOrder(ordered, ordered.get(0).getStandName(),
                ordered.get(0).getBrandName(), latitude, longitude, recommendType);
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
            for (int i = 0; i < array.length(); i++) {
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
                        ObjectMapper mapper = new ObjectMapper();
                        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        try {
                            recommendations = mapper.readValue(response.get("recommendations").toString(),
                                    new TypeReference<List<Recommendation>>() {});
                            //orderReceived= mapper.readValue(response.get("order").toString(), CommonOrder.class);
                            orderReceived = mapper.readerFor(CommonOrder.class).readValue(response.get("order").toString());
                            orderReceived.setTotalPrice(totalPrice);
                            orderReceived.setPrices(ordered);
                            orderReceived.setTotalCount(cartCount);
                            // TODO: add ALL menuItem information to the orderItems!

                            // Add recommendation stands to the spinner
                            if (recommendations.size() > 0) standListAdapter.remove("No stands available");
                            for (Recommendation i : recommendations) {
                                // If specific stand is part of recommendation, link recommendation with specific stand
                                if (specificStand.equals(i.getStandName()) && specificBrand.equals(i.getBrandName())) {
                                    specificRecommendation = i;
                                } else {
                                    standListAdapter.add(i.getStandName());
                                }
                            }
                            // If no specific stand was chosen, update the view
                            if (specificStand.equals("")) {
                                chosenRecommend = 0;
                                showRecommendation(0);
                            }
                            // If specific stand is part of recommendations, updates its view
                            else if (specificRecommendation != null) {
                                chosenRecommend = recommendations.indexOf(specificRecommendation);
                                showSpecificStand();
                            }

                        } catch (JsonProcessingException | JSONException e) {
                            Log.v("JSON exception", "JSON exception in confirmActivity");
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
            public @NonNull
            Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Function that updates the textViews to show the received recommendation info
     * @param i the number of the recommendation in the recommendation list
     */
    @SuppressLint("SetTextI18n")
    public void showRecommendation(int i) {
        if(recommendations != null) {
            if (recommendations.size() > 0) {
                // Set expected time for order
                // timestamp in seconds, calendar in milliseconds
                long timestamp = recommendations.get(i).getTimeEstimate() * 1000;
                timestamp += orderReceived.getStartTime().toInstant().toEpochMilli();
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Europe/Brussels"));
                orderReceived.setExpectedTime(zonedDateTime);

                // Display the chosen recommendation from the recommendation list
                TextView recommend = findViewById(R.id.stand_recommend);
                recommend.setText(R.string.stand_recommendation);

                TextView recommendText = findViewById(R.id.stand_recommend_text);
                recommendText.setText(recommendations.get(i).getStandName());
                recommendText.setTypeface(recommendText.getTypeface(), Typeface.BOLD);

                TextView distanceText = findViewById(R.id.recommend_distance_text);
                distanceText.setVisibility(View.VISIBLE);

                TextView distance = findViewById(R.id.recommend_distance);
                distance.setText(Math.round(recommendations.get(i).getDistance()) + " meter");
                distance.setVisibility(View.VISIBLE);

                TextView remainingTimeText = findViewById(R.id.recommend_time_text);
                remainingTimeText.setVisibility(View.VISIBLE);

                TextView remainingTime = findViewById(R.id.recommend_time);
                remainingTime.setText(recommendations.get(i).getTimeEstimate()/60 + " minute(s)");
                remainingTime.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Display the user specific chosen stand, if available
     */
    public void showSpecificStand() {
        // Display the specific stand chosen by the user
        if (specificRecommendation != null) { // specific stand is part of the recommendations
            showRecommendation(recommendations.indexOf(specificRecommendation));
        } else {
            TextView recommendText = findViewById(R.id.stand_recommend_text);
            recommendText.setText(specificStand);
            recommendText.setTypeface(recommendText.getTypeface(), Typeface.BOLD);

            TextView distanceText = findViewById(R.id.recommend_distance_text);
            distanceText.setVisibility(View.GONE);
            TextView distance = findViewById(R.id.recommend_distance);
            distance.setVisibility(View.GONE);

            TextView remainingTimeText = findViewById(R.id.recommend_time_text);
            remainingTimeText.setVisibility(View.GONE);
            TextView remainingTime = findViewById(R.id.recommend_time);
            remainingTime.setVisibility(View.GONE);
        }

        // Set the recommendation text to specific stand chosen text
        TextView recommend = findViewById(R.id.stand_recommend);
        recommend.setText(R.string.specific_stand_chosen);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An item was selected in the spinner
        if (!adapterView.getItemAtPosition(i).equals("No stands available")) {

            if (specificStand.equals("")) { // If user has not selected all his items from a specific stand
                chosenRecommend = i;
                showRecommendation(i);
            } else { // If user has selected all his items from a specific stand
                if (i == 0) { // no recommendations available
                    if (specificRecommendation == null) { // no specific recommendation available
                        chosenRecommend = -1;
                    } else { // specific recommendation available
                        chosenRecommend = recommendations.indexOf(specificRecommendation);
                    }
                    showSpecificStand();

                } else if (i > 0) { // other recommendations available
                    if (specificRecommendation == null) { // no specific recommendation available
                        chosenRecommend = i - 1;
                        showRecommendation(i - 1);
                    } else {
                        if (i <= recommendations.indexOf(specificRecommendation)) {
                            chosenRecommend = i - 1;
                            showRecommendation(i - 1);
                        } else {
                            chosenRecommend = i;
                            showRecommendation(i);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
