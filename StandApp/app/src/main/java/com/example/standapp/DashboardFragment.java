package com.example.standapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DashboardFragment extends Fragment {

    // This class is not used any more

    private ListView listView;
    private ArrayList<DashboardItem> items = new ArrayList<DashboardItem>();
    private DashboardListViewAdapter adapter;
    private Spinner spinner;
    private Button addButton;
    private Button submitButton;
    private HashMap<String,DashboardItem> hash_snacks;
    private EditText standname;
    private EditText brandname;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_manager_dashboard, container, false);
        View view_profile = inflater.inflate(R.layout.fragment_profile, container, false);
        standname = view_profile.findViewById(R.id.editText_standname);
        brandname = view_profile.findViewById(R.id.editText_brandname);

        addButton = view.findViewById(R.id.add_button);
        submitButton = view.findViewById(R.id.submit_button);
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.snacks, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        listView = view.findViewById(R.id.listView_dashboard);

        //adapter = new DashboardListViewAdapter(getActivity(), items);
        //listView.setAdapter(adapter);
        initHash();

        // When you click on the Add button, this will add the chosen snack in the dashboard
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected_item = spinner.getSelectedItem().toString();
                DashboardItem item = new DashboardItem(hash_snacks.get(selected_item));
                items.add(item);
                adapter.notifyDataSetChanged();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create JSON Object to send to the server
                final JSONObject js = new JSONObject();
                JSONArray js_value = new JSONArray(); //[brandname,lon,lat]
                double longitude = 360.0;
                double latitude = 360.0;

                try {
                    js_value.put(brandname.getText().toString()); //first the brandname is added, after that the two coordinates are added in the next lines
                    js_value.put(longitude); //longitude -> TODO: hardcoded currently, fix later
                    js_value.put(latitude); //latitude -> TODO: hardcoded currently, fix later
                    js.put(standname.getText().toString(), js_value);

                    for (DashboardItem i: items) {
                        int new_count = Integer.parseInt(i.getCount());
                        if (new_count == 0) continue; //if there are 0 items in stock, then no need to send it to the server
                        int prep_time = Integer.parseInt(i.getPrep_time());
                        float price = Float.parseFloat(i.getPrice());

                        JSONArray js_item_values = new JSONArray();
                        js_item_values.put(price);
                        js_item_values.put(prep_time);
                        js_item_values.put(new_count);
                        js_item_values.put(i.getCategory());
                        js_item_values.put(i.getDescription());

                        js.put(i.getTitle(), js_item_values);
                        i.setCount("0");
                    }
                    adapter.notifyDataSetChanged();
                    if (js.length() == 1) {
                        Toast mToast = Toast.makeText(getContext(), "Nothing to send!", Toast.LENGTH_SHORT);
                        mToast.show();
                        return; //if no items were added, then don't send anything
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "http://cobol.idlab.ugent.be:8091/addstand"; // TODO: fix url

                //POST
                StringRequest jsonRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast mToast = Toast.makeText(getContext(), response, Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast mToast = Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        return js.toString().getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }
                };
                //Add the request to the RequestQueue
                queue.add(jsonRequest);
            }
        });


        // When you click long on an item of the menu, the item gets deleted, but you will first receive a notification asking you if you are sure you want to delete it
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Are you sure?");
                alert.setMessage("Do you want to delete this item");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                items.remove(pos);
                                adapter.notifyDataSetChanged();
                            }
                        });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });


        // When you click once on an item, you will be able to edit it
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditBox(items.get(position).getTitle(), items.get(position).getPrice(), items.get(position).getCount(), position);
            }
        });
        return view;
    }

    /**
     * This function edits the title and price of the item
     * @param oldTitle the title of the item before you edit it
     * @param oldPrice the price of the item before you edit it
     * @param position the position of the item within the menu
     */
    private void showEditBox(String oldTitle, String oldPrice, String oldCount, final int position) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setTitle("Edit Box");
        dialog.setContentView(R.layout.edit_box);
        final EditText editTitle = dialog.findViewById(R.id.editText_title);
        editTitle.setText(oldTitle);
        final EditText editPrice = dialog.findViewById(R.id.editText_price);
        editPrice.setText(oldPrice);
        final EditText editCount = dialog.findViewById(R.id.editText_count);
        editCount.setText(oldCount);

        Button editB = dialog.findViewById(R.id.button_edit);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.get(position).setTitle(editTitle.getText().toString());
                items.get(position).setPrice(editPrice.getText().toString());
                items.get(position).setCount(editCount.getText().toString());

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * This initializes all possible snacks that the stand manager will find in the spinner within the manager dashboard
     */
    private void initHash() {
        hash_snacks = new HashMap<String,DashboardItem>();

        DashboardItem burger = new DashboardItem(R.drawable.burger, "Burger", "3.0","150","0","","");
        DashboardItem doughnut = new DashboardItem(R.drawable.doughnut, "Doughnut", "3.5","150","0","","");
        DashboardItem hot_dog = new DashboardItem(R.drawable.hot_dog, "Hot dog", "4.0","150","0","","");
        DashboardItem large_fries = new DashboardItem(R.drawable.large_fries, "Large fries", "4.0","150","0","","");
        DashboardItem medium_fries = new DashboardItem(R.drawable.medium_fries, "Medium fries", "3.0","150","0","","");
        DashboardItem small_fries = new DashboardItem(R.drawable.small_fries, "Small fries", "2.0","150","0","","");
        DashboardItem pizza = new DashboardItem(R.drawable.pizza, "Pizza", "8.0","150","0","","");
        DashboardItem sandwich = new DashboardItem(R.drawable.sandwich, "Sandwich", "3.5","150","0","","");
        DashboardItem toast = new DashboardItem(R.drawable.toast, "Toast", "2.0","150","0","","");
        DashboardItem juice = new DashboardItem(R.drawable.juice, "Juice", "5.0","150","0","","");

        hash_snacks.put("Burger", burger);
        hash_snacks.put("Doughnut", doughnut);
        hash_snacks.put("Hot dog", hot_dog);
        hash_snacks.put("Large fries", large_fries);
        hash_snacks.put("Medium fries", medium_fries);
        hash_snacks.put("Small fries", small_fries);
        hash_snacks.put("Pizza", pizza);
        hash_snacks.put("Sandwich", sandwich);
        hash_snacks.put("Toast", toast);
        hash_snacks.put("Juice", juice);
    }
}
