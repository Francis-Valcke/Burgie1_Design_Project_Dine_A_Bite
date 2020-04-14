package com.example.attendeeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;

import static com.example.attendeeapp.ServerConfig.AUTHORIZATION_TOKEN;


public class MapActivity extends AppCompatActivity {

    protected Map<String, Map<String, Double>> standLocations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        requestStandLocations();
        Toast toast = Toast.makeText(MapActivity.this, standLocations.toString(), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    Toast toast = Toast.makeText(MapActivity.this, "Error!", Toast.LENGTH_SHORT);
                    toast.show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(MapActivity.this, "Error getting stand locations", Toast.LENGTH_SHORT);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
