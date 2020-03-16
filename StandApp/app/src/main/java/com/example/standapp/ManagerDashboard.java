package com.example.standapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerDashboard extends AppCompatActivity {

    private ListView listView;
    private List<DashboardItem> items;
    private DashboardListViewAdapter adapter;
    private Spinner spinner;
    private Button addButton;
    private Button deleteButton;
    private Button submitButton;
    private HashMap<String,DashboardItem> hash_snacks;

    /**
     * This onCreate function is the first function that will be run when the Manager Dashboard opens up
     * @param savedInstanceState the instance that was saved since last time that you have closed the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        addButton = (Button) findViewById(R.id.add_button);
        submitButton = (Button) findViewById(R.id.submit_button);
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.snacks, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        listView = (ListView) findViewById(R.id.listView_dashboard);
        items = new ArrayList<DashboardItem>();
        adapter = new DashboardListViewAdapter(this, items);
        listView.setAdapter(adapter);
        initHash();

        /**
         * When you click on the Add button, this will add the chosen snack in the dashboard
         */
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected_item = spinner.getSelectedItem().toString();
                DashboardItem item = hash_snacks.get(selected_item);
                items.add(item);
                adapter.notifyDataSetChanged();

                // Create JSON Object to send to the server
                /*JSONObject food_item = new JSONObject();
                try {
                    food_item.put(item.getTitle(), item.getPrice());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(ManagerDashboard.this);
                String url = "";

                //Sen
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, food_item, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast mToast = Toast.makeText(ManagerDashboard.this, "Ordering succesful!", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast mToast = Toast.makeText(ManagerDashboard.this, "Ordering failed", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                });*/
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create JSON Object to send to the server
                JSONObject js = new JSONObject();
                JSONArray js_location = new JSONArray(); //[lon,lat]

                try {
                    js_location.put(360); //longitude -> TODO: hardcoded currently, fix later
                    js_location.put(360);//latitude -> TODO: hardcoded currently, fix later
                    js.put("Elberds Burgers", js_location);

                    for (DashboardItem i: items) {
                        JSONArray js_item_values = new JSONArray();
                        js_item_values.put(i.getPrice());
                        js_item_values.put(i.getPreptime());
                        js_item_values.put(i.getCount());
                        js_item_values.put(i.getCategory());
                        js_item_values.put(i.getDescription());

                        js.put(i.getTitle(), js_item_values);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(ManagerDashboard.this);
                String url = "http://cobol.idlab.ugent.be:8092/standmenu?standname=food1"; // TODO: fix url

                //Sen
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, js, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast mToast = Toast.makeText(ManagerDashboard.this, "Submission succesful!", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast mToast = Toast.makeText(ManagerDashboard.this, "Submission failed", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> headers = new HashMap<String,String>();
                        headers.put("Content-Type","application/json");
                        return headers;
                    }
                };
                //Add the request to the RequestQueue
                queue.add(jsonRequest);
            }
        });

        /**
         * When you click long on an item of the menu, the item gets deleted, but you will first receive a notification asking you if you are sure you want to delete it
         */
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                new AlertDialog.Builder(ManagerDashboard.this)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this item")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        items.remove(pos);
                                        adapter.notifyDataSetChanged();
                                    }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        /**
         * When you click once on an item, you will be able to edit it
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditBox(items.get(position).getTitle(), items.get(position).getPrice(), items.get(position).getCount(), position);
            }
        });
    }

    /**
     * This function edits the title and price of the item
     * @param oldTitle the title of the item before you edit it
     * @param oldPrice the price of the item before you edit it
     * @param position the position of the item within the menu
     */
    public void showEditBox(String oldTitle, String oldPrice, String oldCount, final int position) {
        final Dialog dialog = new Dialog(ManagerDashboard.this);
        dialog.setTitle("Edit Box");
        dialog.setContentView(R.layout.edit_box);
        final EditText editTitle = (EditText)dialog.findViewById(R.id.editText_title);
        editTitle.setText(oldTitle);
        final EditText editPrice = (EditText)dialog.findViewById(R.id.editText_price);
        editPrice.setText(oldPrice);
        final EditText editCount = (EditText)dialog.findViewById(R.id.editText_count);
        editCount.setText(oldCount);

        Button editB = (Button)dialog.findViewById(R.id.button_edit);
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
     * The Dashboard item is a class consisting of the icon, title and price of the snack within in the menu (e.g., Pizza with price of 3 euros etc.)
     */
    class DashboardItem {
        public int icon;
        public String title;
        public String price;
        public String preptime;
        public String count;
        public String category;
        public String description;

        DashboardItem(int icon, String title, String price, String preptime, String count, String category, String description) {
            this.icon = icon; //thumbnail for item
            this.title = title; //name of item
            this.price = price; //price of item
            this.preptime = preptime; //preparation time of item
            this.count = count; //amount of items in stock (count=-1 if no items in stock)
            this.category = category; //category that the item belongs to
            this.description = description; //description of the item
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getPreptime() {
            return preptime;
        }

        public void setPreptime(String preptime) {
            this.preptime = preptime;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
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
