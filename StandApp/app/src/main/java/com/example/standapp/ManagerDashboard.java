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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManagerDashboard extends AppCompatActivity {

    private ListView listView;
    private List<DashboardItem> items;
    private DashboardListViewAdapter adapter;
    private Spinner spinner;
    private Button addButton;
    private Button deleteButton;
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
                items.add(hash_snacks.get(selected_item));
                adapter.notifyDataSetChanged();
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
                showEditBox(items.get(position).getTitle(), items.get(position).getPrice(), position);
            }
        });
    }

    /**
     * This function edits the title and price of the item
     * @param oldTitle the title of the item before you edit it
     * @param oldPrice the price of the item before you edit it
     * @param position the position of the item within the menu
     */
    public void showEditBox(String oldTitle, String oldPrice, final int position) {
        final Dialog dialog = new Dialog(ManagerDashboard.this);
        dialog.setTitle("Edit Box");
        dialog.setContentView(R.layout.edit_box);
        final EditText editTitle = (EditText)dialog.findViewById(R.id.editText_title);
        editTitle.setText(oldTitle);
        final EditText editPrice = (EditText)dialog.findViewById(R.id.editText_price);
        editPrice.setText(oldPrice);
        Button editB = (Button)dialog.findViewById(R.id.button_edit);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.get(position).setTitle(editTitle.getText().toString());
                items.get(position).setPrice(editPrice.getText().toString());
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

        DashboardItem(int icon, String title, String price) {
            this.icon = icon;
            this.title = title;
            this.price = price;
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
    }

    /**
     * This initializes all possible snacks that the stand manager will find in the spinner within the manager dashboard
     */
    private void initHash() {
        hash_snacks = new HashMap<String,DashboardItem>();

        DashboardItem burger = new DashboardItem(R.drawable.burger, "Burger", "3");
        DashboardItem doughnut = new DashboardItem(R.drawable.doughnut, "Doughnut", "3.5");
        DashboardItem hot_dog = new DashboardItem(R.drawable.hot_dog, "Hot dog", "4");
        DashboardItem large_fries = new DashboardItem(R.drawable.large_fries, "Large fries", "4");
        DashboardItem medium_fries = new DashboardItem(R.drawable.medium_fries, "Medium fries", "3");
        DashboardItem small_fries = new DashboardItem(R.drawable.small_fries, "Small fries", "2");
        DashboardItem pizza = new DashboardItem(R.drawable.pizza, "Pizza", "8");
        DashboardItem sandwich = new DashboardItem(R.drawable.sandwich, "Sandwich", "3.5");
        DashboardItem toast = new DashboardItem(R.drawable.toast, "Toast", "2");
        DashboardItem juice = new DashboardItem(R.drawable.juice, "Juice", "5");

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
