package com.example.attendeeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Handles all the cart items in the cart list
 */
public class CartItemAdapter  extends BaseAdapter {
    private ArrayList<MenuItem> list = new ArrayList<MenuItem>();
    private Context context;

    public CartItemAdapter(ArrayList<MenuItem> list,Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.cart_item_material, null);
        }

        //Handle TextView to display one cart item name, if this name has a stand, display it too
        TextView listItemText = (TextView)view.findViewById(R.id.cart_item);
        String name = list.get(position).getFoodName();
        if(!list.get(position).getStandName().equals("")) name += " (" + list.get(position)
                                                                        .getStandName() + ")";
        listItemText.setText(name);

        //Handle TextView to display one cart item price
        TextView listItemPrice = (TextView)view.findViewById(R.id.cart_item_price);
        listItemPrice.setText(list.get(position).getPriceEuro());

        //Handle TextView to display one cart item count
        TextView listItemCount = (TextView)view.findViewById(R.id.cart_item_count);
        listItemCount.setText(String.valueOf(list.get(position).getCount()));


        return view;
    }
}
