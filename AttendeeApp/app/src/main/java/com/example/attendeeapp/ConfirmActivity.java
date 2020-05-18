package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.CommonFood;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderItem;
import com.example.attendeeapp.json.Recommendation;
import com.example.attendeeapp.json.SuperOrderRec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * <p>
 * The FLOW in this activity is as follows:
 * - The order from CartActivity is split up over the different brands
 * - For each brand, an order is placed at the server and recommendations are fetched
 * - If an order of one brand is ordered from the SAME specific stand, the order is placed with /placeOrder
 * If an order of one brand has no specific stand for all order items, the order is placed with /placeSuperOrder
 * If this SuperOrder cannot be made in one stand (although the brand is the same), the server can split it up
 * => if the order is split up, the user must be notified
 * - Next the user must confirm a stand for the returned order from the server and the confirmed order is saved locally
 * - The confirmation of stands must be done for each brand separately
 * If the order (of one brand) was placed with /placeSuperOrder and split up,
 * the user must confirm all stands for the split up order
 * - If all orders have a confirmed stand, the confirmedOrder list is sent over to orderActivity
 * to be sent to the server for confirmation
 */
public class ConfirmActivity extends ToolbarActivity implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> standListAdapter;
    private ArrayList<CommonFood> ordered; // current ordered items
    private List<Recommendation> recommendations = null; // current stand recommendations
    private CommonOrder orderReceived = null; // current stand order
    // index in the recommendation list of the currently chosen recommendation in the spinner
    private int chosenRecommend = -1;
    // if a recommendation of the recommendation list contains the specific chosen stand, it is saved here
    private Recommendation specificRecommendation = null;

    private String specificStand;
    private String specificBrand;
    private HashMap<String, ArrayList<CommonFood>> brandItemMap = new HashMap<>();
    private Iterator<Map.Entry<String, ArrayList<CommonFood>>> mapIterator;
    private ArrayList<CommonOrder> confirmedOrders = new ArrayList<>();

    // current stand confirmation number for a certain brand
    private int confirmNumber = 1;
    // current stand confirmation number for a certain brand,
    // when an order of the same brand is split up over multiple stands
    private int confirmSplitOrderNumber = 0;
    // the recommendations of a split order of the same brand
    private List<SuperOrderRec> splitOrderRecommendations = null;

    private Toast mToast = null;
    private AlertDialog mDialog = null;
    private CommonOrder.RecommendType recommendType = null;

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
        recommendType = (CommonOrder.RecommendType) getIntent().getSerializableExtra("recType");
        lastLocation = getIntent().getParcelableExtra("location");

        // Divide items into different brands
        for (CommonFood item : ordered) {
            ArrayList<CommonFood> localList = brandItemMap.get(item.getBrandName());
            if (localList != null) {
                localList.add(item);
            } else {
                localList = new ArrayList<>();
                localList.add(item);
                brandItemMap.put(item.getBrandName(), localList);
            }
        }

        // Create a spinner item for the different stands
        Spinner spinner = findViewById(R.id.stand_recommended_spinner);
        spinner.setOnItemSelectedListener(this);

        mapIterator = brandItemMap.entrySet().iterator();
        // Let user confirm for items of the first same brand
        confirmNextStand();

        Button chooseRecommendButton = findViewById(R.id.button_confirm_stand);
        chooseRecommendButton.setOnClickListener(v -> {
            boolean noRecommend = true;
            if (recommendations != null && orderReceived != null) {
                // An order could be received to the server
                if (recommendations.size() > 0 && (
                        (specificRecommendation != null || chosenRecommend != -1)
                                || specificStand.equals(""))) {
                    noRecommend = false;

                    // Add order with confirmed stand to confirmedOrderList
                    orderReceived.setStandName(recommendations.get(chosenRecommend).getStandName());
                    orderReceived.setBrandName(recommendations.get(chosenRecommend).getBrandName());
                    confirmedOrders.add(orderReceived);

                    if (confirmNumber - 1 == brandItemMap.keySet().size() &&
                            confirmSplitOrderNumber == splitOrderRecommendations.size()) {
                        // Continue to overview with confirmed stands for the orders
                        Intent listIntent = new Intent(ConfirmActivity.this, OrderActivity.class);
                        listIntent.putExtra("orderList", confirmedOrders);
                        startActivity(listIntent);
                    } else {
                        if (splitOrderRecommendations.size() > confirmSplitOrderNumber) {
                            // Continue confirming next stand for split order
                            confirmNextSplitStand();
                        } else {
                            // Continue confirming stands for the next brand
                            confirmNextStand();
                        }
                    }
                } else if (recommendations.size() > 0) {
                    // specificRecommendation is not part of the returned recommendations
                    noRecommend = false;

                    // Alert user if he not better like the recommended stand
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmActivity.this);

                    builder.setPositiveButton("Continue", (dialog, id) -> {
                        // User clicked Continue button
                        dialog.cancel();

                        // TODO: update expected timings when
                        //  order from specific stand without recommendation is made !! important
                        //  (need timing for the order from server)
                        // Add order with confirmed stand to confirmedOrderList
                        orderReceived.setStandName(specificStand);
                        confirmedOrders.add(orderReceived);

                        if (confirmNumber - 1 == brandItemMap.keySet().size() &&
                                confirmSplitOrderNumber == splitOrderRecommendations.size()) {
                            // Continue to overview with confirmed stands for the orders
                            Intent listIntent = new Intent(ConfirmActivity.this, OrderActivity.class);
                            listIntent.putExtra("orderList", confirmedOrders);
                            startActivity(listIntent);
                        } else {
                            // Continue confirming stands for the next brand
                            if (splitOrderRecommendations.size() > confirmSplitOrderNumber) {
                                // Continue confirming next stand for split order
                                confirmNextSplitStand();
                            } else {
                                // Continue confirming stands for the next brand
                                confirmNextStand();
                            }
                        }

                    });
                    builder.setNegativeButton("Cancel", (dialog, id) -> {
                        // User cancelled the dialog
                        dialog.cancel();
                    });

                    builder.setMessage("You have a recommendation available." +
                            "\nAre you sure you want to choose your own stand?")
                            .setTitle("Continue with chosen stand");
                    if (mDialog != null) mDialog.cancel();
                    mDialog = builder.create();
                    mDialog.show();
                }
            }
            if (noRecommend) {
                if (!specificStand.equals("")) {
                    showToast("You cannot continue, if there are recommendations, check your balance");
                } else {
                    showToast("No stand available");
                }
            }
        });
    }

    /**
     * Requests a recommendation for the items of the next brand available in the brandItemMap
     * Method is called as much as there are different brands in the total order of the user
     */
    private void confirmNextStand() {
        // Get next brand - orderItems entry of the map
        Map.Entry<String, ArrayList<CommonFood>> brandPair = mapIterator.next();
        String brandKey = brandPair.getKey();
        // Overwrite ordered to be used in request(Super)OrderRecommend
        // Now contains all orderItems that have the same certain brand
        ordered = brandPair.getValue();

        // Display which brand and number of the different brands is currently being confirmed
        TextView confirmNumberTxt = findViewById(R.id.confirm_number);
        confirmNumberTxt.setText("(" + confirmNumber + "/" + brandItemMap.keySet().size() + ")");
        confirmNumber++;

        TextView confirmBrandTxt = findViewById(R.id.confirm_brand);
        confirmBrandTxt.setText(brandKey);

        // Should only be visible if order from one brand has to be split up over multiple stands
        // This is done in confirmNextSplitStand
        TextView confirmBrandNumberTxt = findViewById(R.id.confirm_brand_number);
        confirmBrandNumberTxt.setVisibility(View.GONE);

        // Check if the user wants to order from a specific stand (all ordered items are from the same stand/brand)
        // If specificStand equals "", no specific stand can be determined, and a superOrder must be made
        specificStand = ordered.get(0).getStandName();
        specificBrand = ordered.get(0).getBrandName();
        for (CommonFood i : ordered.subList(1, ordered.size())) {
            if (!specificStand.equals(i.getStandName())) {
                specificStand = "";
                break;
            }
        }

        resetFields();

        // Reset initial values as if ConfirmActivity was restarted
        TextView recommendText = findViewById(R.id.stand_recommend_text);
        recommendText.setText(R.string.no_recommendation);
        recommendText.setTypeface(null, Typeface.ITALIC);

        recommendations = null;
        orderReceived = null;
        chosenRecommend = -1;
        confirmSplitOrderNumber = 0;
        splitOrderRecommendations = new ArrayList<>();
        specificRecommendation = null;

        if (specificStand.equals("")) {
            // If order items may come from different stands, request a superOrder
            Log.d("OrderMessage", "Requesting super order");
            requestSuperOrderRecommend();
        } else {
            // If all items of the order are chosen from the same stand, no superOrder is required
            Log.d("OrderMessage", "Requesting normal order");
            requestOrderRecommend();
        }
    }

    /**
     * Make distance and time of recommendation invisible until recommendation comes available
     * Initializes a new spinner for the current stand recommendations
     * Reset the current order items to be confirmed
     */
    private void resetFields() {
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

        // Initiate the spinner item adapter
        standListAdapter = new ArrayAdapter<>(this,
                R.layout.stand_spinner_item, new ArrayList<String>());
        spinner.setAdapter(standListAdapter);
        if (!specificStand.equals("")) {
            standListAdapter.add(specificStand + " (Your stand)");
        } else {
            standListAdapter.add("No stands available");
        }

        // Reset order items to confirm view
        TextView confirmItemsText = findViewById(R.id.confirm_order_items_text);
        confirmItemsText.setVisibility(View.VISIBLE);
        LinearLayout listView = findViewById(R.id.confirm_list);
        listView.removeAllViews();
    }

    /**
     * Called when an order of the same brand has been split up over multiple stand by the server
     * Used to display and handle the next stand confirmation of the split order
     */
    private void confirmNextSplitStand() {

        resetFields();

        // Reset initial fields
        recommendations = null;
        orderReceived = null;
        chosenRecommend = -1;
        specificRecommendation = null;
        handleReceivedRecommendation(splitOrderRecommendations.get(confirmSplitOrderNumber));

        confirmSplitOrderNumber++;

        // Should only be visible if order from one brand has to be split up over multiple stands
        // Display which split stand number is currently being confirmed
        TextView confirmBrandNumberTxt = findViewById(R.id.confirm_brand_number);
        confirmBrandNumberTxt.setVisibility(View.VISIBLE);
        confirmBrandNumberTxt.setText("(" + confirmSplitOrderNumber + "/" + splitOrderRecommendations.size() + ")");

        showSplitOrderAlert();

    }

    private void showSplitOrderAlert() {
        // Alert user that the server has split up his order
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmActivity.this);

        builder.setPositiveButton("Ok", (dialog, id) -> {
            // User clicked Ok button
            dialog.cancel();
        });

        String message = "Your order of brand \"" + orderReceived.getBrandName()
                + "\" has been split up." +
                "\nThe items you are about to confirm for this brand are show below.";
        builder.setMessage(message)
                .setTitle("Order has been split up!");
        if (mDialog != null) mDialog.cancel();
        mDialog = builder.create();
        mDialog.show();
    }

    /**
     * Sends order of user of the same brand to the server in JSON to request a recommendation
     * when ALL items are from the same specific stand (and brand)
     * <p>
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
        CommonOrder orderSent = new CommonOrder(ordered, specificStand, specificBrand, latitude, longitude, recommendType);
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
                response -> {
                    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    BetterResponseModel<JsonNode> responseModel = null;
                    try {
                        responseModel = mapper.readValue(response.toString(), new TypeReference<BetterResponseModel<JsonNode>>() {
                        });
                    } catch (JsonProcessingException e) {
                        showToast("Exception parsing response from server");
                        return;
                    }
                    assert responseModel != null;

                    // Exception from server
                    if (!responseModel.isOk()) {
                        showToast(responseModel.getException().getMessage());
                        return;
                    }


                    SuperOrderRec orderRec=null;
                    try {
                        orderRec = mapper.readValue(responseModel.getPayload().toString(), SuperOrderRec.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        showToast("Error while parsing response from server");
                        return;
                    }

                    // Response is ok, no exception on server
                    handleReceivedRecommendation(orderRec);
                }, error -> {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(ConfirmActivity.this, "Recommendation could not be fetched.",
                    Toast.LENGTH_SHORT);
            mToast.show();
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
     * Sends order items of user of the same brand to the server in JSON to request a recommendation
     * A SuperOrder is required when all ordered items may be from different stands (but the same brand)
     * <p>
     * Send a JSON object with ordered items and user location
     * Format: CommonOrder converted to JSON
     * Location is (360, 360) when user location is unknown
     */
    private void requestSuperOrderRecommend() {

        //360 is value for location unknown
        double latitude = 360;
        double longitude = 360;
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        // Make JSON Object with ordered items and location
        CommonOrder orderSent = new CommonOrder(ordered, specificStand, specificBrand, latitude, longitude, recommendType);
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
        String url = ServerConfig.OM_ADDRESS + "/placeSuperOrder";
        final JSONObject body = jsonOrder;

        // Request recommendation from server for sent order (both in JSON)
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {

                    BetterResponseModel<List<SuperOrderRec>> responseModel=null;
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        responseModel = mapper
                                .readValue(response.toString(), new TypeReference<BetterResponseModel<List<SuperOrderRec>>>() {
                                });
                    } catch (JsonProcessingException e) {
                        Log.v("JSON exception", "JSON exception in confirmActivity");
                        e.printStackTrace();
                        showToast("Exception while parsing response for superorder");
                        return;
                    }

                    if(responseModel!=null){
                        if(responseModel.isOk()){

                            splitOrderRecommendations.addAll(responseModel.getPayload());

                            if (responseModel.getPayload().size() > 1) {
                                confirmNextSplitStand();
                            } else {
                                // Order has not been split up
                                confirmSplitOrderNumber = 1;
                                handleReceivedRecommendation(splitOrderRecommendations.get(0));
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
                        }
                        else{
                            showToast(responseModel.getException().getMessage());
                        }

                    }
                    else{
                        showToast("Exception while receiving response from superorder");
                    }


                }, error -> {
            showToast("Recommendation could not be fetched.");
        }) { // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }

            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonRequest);
    }

    /**
     * Handles one recommendation/order pair to handle the confirmation of a stand for that order
     * Called by the place(Super)Order http request response listeners and confirmNextSplitStand
     *
     * @param response: jsonNode containing the recommendation(s) and the order
     */
    private void handleReceivedRecommendation(SuperOrderRec response) {
        recommendations = new ArrayList<>(response.getRecommendations());
        orderReceived = response.getOrder();

        //TODO: remove when checked that setting prices/cartCount/totalPrice is redundant
        orderReceived.setPrices(ordered);
        // Recalculate the totalPrice and total cartCount of the order
        // if order was split up, recalculation is definitely required
        int cartCount = 0;
        BigDecimal totalPrice = new BigDecimal(0);
        for (CommonOrderItem i : orderReceived.getOrderItems()) {
            cartCount += i.getAmount();

            totalPrice = totalPrice.add(i.getPrice().multiply(new BigDecimal(i.getAmount())));
        }
        orderReceived.setTotalPrice(totalPrice);
        orderReceived.setTotalCount(cartCount);
        orderReceived.setBrandName(specificBrand);
        // TODO: add ALL menuItem information to the orderItems!

        // Check if recommendation contains the own chosen stand
        // TODO: may become redundant when the complete list is returned
        for (Recommendation i : recommendations) {
            // If specific stand is part of recommendation, link recommendation with specific stand
            if (specificStand.equals(i.getStandName()) && specificBrand.equals(i.getBrandName())) {
                specificRecommendation = i;
                break;
            }
        }

        // Add specific recommendation if available
        if (recommendations.size() > 0) standListAdapter.remove("No stands available");
        if (specificRecommendation != null) {
            standListAdapter.remove(specificStand + " (Your stand)");
            standListAdapter.add(specificRecommendation.getRank() + ". " + specificStand + " (Your stand)");
        }
        // Add other recommendations to the spinner
        for (Recommendation i : recommendations) {
            if (!i.equals(specificRecommendation)) {
                standListAdapter.add(i.getRank() + ". " + i.getStandName());
            }
        }

        // If no specific stand was chosen, update the view
        if (specificStand.equals("")) {
            chosenRecommend = 0;
            showRecommendation(0);
        }
        // If specific stand is part of recommendations, updates the view with specific stand recommendation
        else if (specificRecommendation != null) {
            chosenRecommend = recommendations.indexOf(specificRecommendation);
            showSpecificStand();
        }

        showOrderDetails();
    }

    /**
     * Display the current items of the order being confirmed
     */
    private void showOrderDetails() {

        TextView confirmItemsText = findViewById(R.id.confirm_order_items_text);
        confirmItemsText.setVisibility(View.GONE);

        LinearLayout listView = findViewById(R.id.confirm_list);

        for (CommonOrderItem i : orderReceived.getOrderItems()) {
            View view = getLayoutInflater().inflate(R.layout.confirm_item_material, null);
            TextView textName = view.findViewById(R.id.confirm_item_name);
            TextView textCount = view.findViewById(R.id.confirm_item_count);
            TextView textPrice = view.findViewById(R.id.confirm_item_price);
            textName.setText(i.getFoodName());
            textCount.setText(i.getAmount() + "");
            textPrice.setText(i.getPriceEuro());
            listView.addView(view);
        }
    }

    /**
     * Function that updates the textViews to show the received recommendation info
     *
     * @param i the number of the recommendation in the recommendation list
     */
    @SuppressLint("SetTextI18n")
    private void showRecommendation(int i) {
        if (recommendations != null) {
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
                remainingTime.setText(recommendations.get(i).getTimeEstimate() / 60 + " minute(s)");
                remainingTime.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Display the user specific chosen stand when a recommendation with this chosen stand
     * is available for the order
     */
    private void showSpecificStand() {
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

    public void showToast(String message) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(ConfirmActivity.this, message,
                Toast.LENGTH_SHORT);
        mToast.show();
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
