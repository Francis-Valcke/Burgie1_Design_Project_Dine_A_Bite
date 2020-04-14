package com.example.attendeeapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    protected Map<String, Map<String, Double>> standLocations = new HashMap<>();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestStandLocations();
        setContentView(R.layout.activity_maps);
        /*// Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);*/
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //        .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestStandLocations();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (standLocations.isEmpty()) {
            LatLng test = new LatLng(-41, 151);
            mMap.addMarker(new MarkerOptions().position(test).title("Heyo"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(test));
        }
        for (String standName : standLocations.keySet()) {
            Map<String, Double> coordinates = standLocations.get(standName);
            System.out.println(standLocations);
            double lat = coordinates.get("latitude");
            double lon = coordinates.get("longitude");
            LatLng newStand = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(newStand).title(standName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newStand));
        }
    }

    /**
     * Get the stand locations from the server
     */
    private void requestStandLocations() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String uri = ServerConfig.OM_ADDRESS + "/standLocations";
        StringRequest request = new StringRequest(Request.Method.GET, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    standLocations = mapper.readValue(response, new TypeReference<Map<String, Map<String, Double>>>() {});
                } catch (JsonProcessingException e) {
                    //TODO: handle exception
                    Toast toast = Toast.makeText(MapsActivity.this, "Error!", Toast.LENGTH_SHORT);
                    toast.show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(MapsActivity.this, "Error getting stand locations", Toast.LENGTH_SHORT);
                toast.show();
            }
        }) {
            // Add JSON headers
            @Override
            public @NonNull
            Map<String, String> getHeaders()  throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", AUTHORIZATION_TOKEN);
                return headers;
            }
        };
        queue.add(request);
    }

}
