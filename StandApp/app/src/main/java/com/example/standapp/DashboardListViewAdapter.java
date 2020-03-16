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

import java.util.List;

public class DashboardListViewAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<ManagerDashboard.DashboardItem> items;

    public DashboardListViewAdapter(Activity context, List<ManagerDashboard.DashboardItem> items) {
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
        ManagerDashboard.DashboardItem  item = items.get(position);
        View view = convertView;


        if (convertView == null) {
            view = inflater.inflate(R.layout.custom_product_listview, null);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.icon_menu);
        TextView title = (TextView) view.findViewById(R.id.textView_title);
        TextView price = (TextView) view.findViewById(R.id.textView_price);
        TextView count = (TextView) view.findViewById(R.id.textView_count_product);

        icon.setImageResource(item.getIcon());
        title.setText(item.getTitle());
        price.setText(item.getPrice());
        count.setText(item.getCount());

        return view;
    }
}
