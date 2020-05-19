package com.example.attendeeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendeeapp.json.CommonFood;

import java.util.ArrayList;

/**
 * Handles all the menu items in a global, stand or category menu list.
 */
public class MenuItemAdapter extends BaseAdapter {

    private ArrayList<CommonFood> list;
    private Context context;
    private OnCartChangeListener cartListener;
    private MenuBottomSheetDialog bottomSheet;

    public MenuItemAdapter(ArrayList<CommonFood> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setCartChangeListener(OnCartChangeListener listener){
        this.cartListener = listener;
    }


    public void putList(ArrayList<CommonFood> l) {
        list = l;
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

    /**
     * Method to get a View that displays a food item at the specified position in the menu.
     */
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Root cannot be parent
            view = inflater.inflate(R.layout.menu_item_material, null);
        }

        // Add expandable bottomSheet for every item
        RelativeLayout reLay = view.findViewById(R.id.menu_item_layout);
        reLay.setOnClickListener(v -> {
            if(bottomSheet != null) bottomSheet.dismiss();
            bottomSheet = new MenuBottomSheetDialog(list.get(position));
            bottomSheet.setCartChangeListener(cartListener);
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(),
                    "bottomSheet");
        });

        // Handle TextView to display one menu item name
        TextView listItemText = view.findViewById(R.id.menu_item);
        listItemText.setText(list.get(position).getName());

        // Handle TextView to display one menu item price
        TextView listItemPrice = view.findViewById(R.id.menu_item_price);
        listItemPrice.setText(list.get(position).getPriceEuro());

        // Handle Button and add onClickListeners for one menu item
        Button plusBtn = view.findViewById(R.id.plus);
        plusBtn.setOnClickListener(v -> {
            // Pass menu item to the cart to (try) to be added
            cartListener.onCartChangedAdd(list.get(position));
        });

        return view;
    }
}