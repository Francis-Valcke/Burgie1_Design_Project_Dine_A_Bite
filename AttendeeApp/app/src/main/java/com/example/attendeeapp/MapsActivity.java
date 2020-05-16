package com.example.attendeeapp;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Activity that show a map of all the stand locations
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    protected Map<String, Map<String, Double>> standLocations = new HashMap<>();

    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestStandLocations();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        for (String standName : standLocations.keySet()) {
            Map<String, Double> coordinates = standLocations.get(standName);
            double lat = coordinates.get("latitude");
            double lon = coordinates.get("longitude");
            if (lat != 360 && lon != 360) {
                LatLng newStand = new LatLng(lat, lon);
                googleMap.addMarker(new MarkerOptions().position(newStand).title(standName));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(newStand));
            }
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        Location lastLocation = (Location) getIntent().getParcelableExtra("locationClient");
        if (lastLocation != null) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

    }

    /**
     * Get the stand locations from the server
     */
    private void requestStandLocations() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String uri = ServerConfig.OM_ADDRESS + "/standLocations";
        StringRequest request = new StringRequest(Request.Method.GET, uri, response -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                BetterResponseModel<Map<String, Map<String, Double>>> responseModel= mapper.readValue(response, new TypeReference<BetterResponseModel<Map<String, Map<String, Double>>>>() {});
                if(!responseModel.isOk()){
                    Toast toast = Toast.makeText(MapsActivity.this,responseModel.getException().getMessage() , Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                standLocations = responseModel.getPayload();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
            } catch (JsonProcessingException e) {
                Log.e("MapsActivity", Objects.requireNonNull(e.getMessage()));
                Toast toast = Toast.makeText(MapsActivity.this, "Error parsing stand locations!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }, error -> {
            Toast toast = Toast.makeText(MapsActivity.this, "Error getting stand locations", Toast.LENGTH_SHORT);
            toast.show();
        }) {
            // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };
        queue.add(request);
    }

}
