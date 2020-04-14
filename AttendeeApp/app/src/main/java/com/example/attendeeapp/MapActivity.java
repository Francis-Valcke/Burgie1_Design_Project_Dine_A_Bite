package com.example.attendeeapp;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends AppCompatActivity {

    private Map<String, Map<String, Double>> standLocations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*// Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true); */
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Get the stand locations from the server
     */
    private void requestStandLocations() {
        String uri = ServerConfig.OM_ADDRESS + "/standLocations";

    }
}
