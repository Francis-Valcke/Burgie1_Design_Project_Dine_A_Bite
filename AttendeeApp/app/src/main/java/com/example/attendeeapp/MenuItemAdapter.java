package com.example.attendeeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Handles all the menu items in the global menu list
 */
public class MenuItemAdapter extends BaseAdapter {

    private static final int MAX_CART_ITEM = 25;
    private ArrayList<MenuItem> list = new ArrayList<MenuItem>();
    private int cartCount;
    private Context context;
    private Toast mToast = null;
    private OnCartChangeListener cartListener;

    public MenuItemAdapter(ArrayList<MenuItem> list,Context context) {
        this.list = list;
        this.context = context;
    }

    public void setCartChangeListener(OnCartChangeListener listener){
        this.cartListener = listener;
    }

    /**
     * Function that returns all menu list items with positive item count
     * @return: ordered menu items
     */
    public ArrayList<MenuItem> getOrderedMenuList() {
        ArrayList<MenuItem> ordered = new ArrayList<MenuItem>();
        for(MenuItem i : list){
            if(i.getCount() > 0) ordered.add(i);
        }
        return ordered;
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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.menu_item_material, null);
        }

        // Handle TextView to display one menu item name
        TextView listItemText = (TextView)view.findViewById(R.id.menu_item);
        listItemText.setText(list.get(position).getItem());

        // Handle TextView to display one menu item price
        TextView listItemPrice = (TextView)view.findViewById(R.id.menu_item_price);
        listItemPrice.setText(list.get(position).getPriceEuro());

        // Handle Button and add onClickListeners for one menu item
        Button plusBtn = (Button)view.findViewById(R.id.plus);

        plusBtn.setOnClickListener(new View.OnClickListener(){
            /**
             * Function that increases the menu item and cart count
             * if an item is selected and handles maximum number of possible items
             * @param v: View of list item
             */
            @Override
            public void onClick(View v) {
                if (cartCount < MAX_CART_ITEM) {
                    try {
                        list.get(position).increaseCount();
                        cartCount++;
                        cartListener.onCartChanged(cartCount);

                        notifyDataSetChanged();
                    } catch (ArithmeticException e){
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(context,"No more than 10 items", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                } else {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(context,"No more than 25 in total", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });

        return view;
    }
}