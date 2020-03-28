package com.example.standapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class DashboardListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<DashboardItem> items;

    DashboardListViewAdapter(Activity context, List<DashboardItem> items) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DashboardItem  item = items.get(position);
        View view = convertView;

        if (convertView == null) {
            view = inflater.inflate(R.layout.menu_item, null);
        }

        TextView name = view.findViewById(R.id.menu_item_name);
        TextView price = view.findViewById(R.id.menu_item_price);
        TextView stock = view.findViewById(R.id.menu_item_stock);
        name.setText(item.getTitle());
        price.setText(item.getPrice());
        stock.setText(item.getCount());

        Button editButton = view.findViewById(R.id.edit_menu_item_button);
        final View finalView = view;
        final View editDialogLayout = inflater.inflate(R.layout.add_menu_item_dialog, null, false);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(finalView.getContext())
                        //.setView(editDialogLayout);
                //dialog.show();
            }
        });

        return view;
    }
}
