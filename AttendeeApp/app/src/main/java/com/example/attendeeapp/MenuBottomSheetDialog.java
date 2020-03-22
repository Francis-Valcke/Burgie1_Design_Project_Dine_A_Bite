package com.example.attendeeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Handles the bottomSheet for a given menuItem that contains more details about the item
 */
public class MenuBottomSheetDialog extends BottomSheetDialogFragment {

    private MenuItem item;
    private OnCartChangeListener cartListener;

    public MenuBottomSheetDialog(MenuItem menuItem) {
        item = menuItem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_bottom_sheet, container,
                            false);

        // Handle TextView to display the menu item name
        TextView listItemText = (TextView)view.findViewById(R.id.menu_item_bottom);
        listItemText.setText(item.getFoodName());

        // Handle TextView to display the menu item price
        TextView listItemPrice = (TextView)view.findViewById(R.id.price_item_bottom);
        listItemPrice.setText(item.getPriceEuro());

        // Handle TextView to display the description
        TextView listItemDescr = (TextView)view.findViewById(R.id.description_bottom);
        listItemDescr.setText(item.getDescription());

        // Handle plus and minus Buttons and add onClickListeners for one menu item
        Button plusBtn = (Button)view.findViewById(R.id.bottom_sheet_plus);
        plusBtn.setOnClickListener(new View.OnClickListener(){
            // Handle Button and add onClickListeners for one menu item
            @Override
            public void onClick(View v) {
                // Pass menu item to the cart to (try) to be added
                cartListener.onCartChangedAdd(item);
            }
        });

        Button minusBtn = (Button)view.findViewById(R.id.bottom_sheet_minus);
        minusBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Pass menu item to the cart to (try) to be removed
                cartListener.onCartChangedRemove(item);
            }
        });

        return view;
    }

    public void setCartChangeListener(OnCartChangeListener listener){
        this.cartListener = listener;
    }
}
