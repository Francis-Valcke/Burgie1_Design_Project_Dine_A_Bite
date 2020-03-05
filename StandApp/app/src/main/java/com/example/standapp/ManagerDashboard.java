package com.example.standapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboard extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private List<String> spinnerText;
    private List<Integer> spinnerImage;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        /*spinnerText = new ArrayList<>();
        spinnerImage = new ArrayList<>();

        //spinnerText.add("Pizza");
        spinnerText.add("Medium Fries");

        //ImageView m = (ImageView) findViewById(R.id.burger);
        //spinnerImage.add(R.drawable.pizza);
        spinnerImage.add(R.drawable.medium_fries);

        spinner = (Spinner)findViewById(R.id.spinner);

        //Use You Custom Adapter
        SpinnerAdapter adapter = new SpinnerAdapter(ManagerDashboard.this, R.layout.custom_spinner_item, spinnerImage, spinnerText);

        //Set Your Custom Adapter To Your Spinner
        spinner.setAdapter(adapter);*/

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.snacks, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

}
