package com.example.standapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.standapp.json.CommonFood;

import java.util.ArrayList;

public class DashboardListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CommonFood> items;
    private Fragment parentFragment;

    DashboardListViewAdapter(Activity context, ArrayList<CommonFood> items, Fragment parentFragment) {
        super();
        this.items = items;
        this.parentFragment = parentFragment;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, final ViewGroup parent) {

        final CommonFood item = items.get(position);
        final int finalPosition = position;
        View view = convertView;

        if (convertView == null) view = inflater.inflate(R.layout.menu_item, parent, false);

        TextView name = view.findViewById(R.id.menu_item_name);
        TextView price = view.findViewById(R.id.menu_item_price);
        TextView stock = view.findViewById(R.id.menu_item_stock);
        name.setText(item.getName());
        price.setText(item.getPrice().toString());
        stock.setText(Integer.toString(item.getStock()));

        // Editing or deleting an already existing menu item in the manager dashboard
        Button editButton = view.findViewById(R.id.edit_menu_item_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Open dialog to fill in information for adding new menu item
                Bundle dialogBundle = new Bundle();
                dialogBundle.putSerializable("menu_item", item);
                dialogBundle.putInt("menu_item_position", finalPosition);
                MenuItemFragment menuItemFragment = new MenuItemFragment();
                menuItemFragment.setArguments(dialogBundle);
                menuItemFragment.show(parentFragment.getChildFragmentManager().beginTransaction(),
                        "menu_item_dialog");

            }
        });

        return view;
    }
}
