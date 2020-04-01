package com.example.standapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DashboardListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<MenuItem> items;

    DashboardListViewAdapter(Activity context, List<MenuItem> items) {
        super();
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MenuItem item = items.get(position);
        View view = convertView;

        if (convertView == null) view = inflater.inflate(R.layout.menu_item, parent, false);

        TextView name = view.findViewById(R.id.menu_item_name);
        TextView price = view.findViewById(R.id.menu_item_price);
        TextView stock = view.findViewById(R.id.menu_item_stock);
        name.setText(item.getFoodName());
        price.setText(item.getPrice().toString());
        stock.setText(Integer.toString(item.getStock()));

        // Editing or deleting an already existing menu item in the manager dashboard
        Button editButton = view.findViewById(R.id.edit_menu_item_button);
        final View finalView = view;
        final View editDialogLayout = inflater.inflate(R.layout.add_menu_item_dialog, parent, false);
        final TextInputEditText nameInput = editDialogLayout.findViewById(R.id.menu_item_name);
        final TextInputEditText priceInput = editDialogLayout.findViewById(R.id.menu_item_price);
        final TextInputEditText stockInput = editDialogLayout.findViewById(R.id.menu_item_stock);
        nameInput.setText(item.getFoodName());
        priceInput.setText(item.getPrice().toString());
        stockInput.setText(Integer.toString(item.getStock()));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(finalView.getContext())
                        .setView(editDialogLayout)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = Objects.requireNonNull(nameInput.getText()).toString();
                                BigDecimal price = new BigDecimal(Objects.requireNonNull(priceInput.getText()).toString());
                                int stock = Integer.parseInt(Objects.requireNonNull(stockInput.getText()).toString());
                                item.setFoodName(name);
                                item.setPrice(price);
                                item.setStock(stock);
                                notifyDataSetChanged();
                                ViewGroup parent = (ViewGroup) editDialogLayout.getParent();
                                if (parent != null) parent.removeView(editDialogLayout);
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                ViewGroup parent = (ViewGroup) editDialogLayout.getParent();
                                if (parent != null) parent.removeView(editDialogLayout);
                            }
                        }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(finalView.getContext())
                                        .setTitle("Delete menu item")
                                        .setMessage("Are you sure?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                items.remove(position);
                                                notifyDataSetChanged();
                                            }
                                        }).setNegativeButton("No", null);
                                alertDialog.show();
                                ViewGroup parent = (ViewGroup) editDialogLayout.getParent();
                                if (parent != null) parent.removeView(editDialogLayout);
                            }
                        });
                dialog.show();
            }
        });

        return view;
    }
}
