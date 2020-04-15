package com.example.attendeeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendeeapp.json.CommonFood;

import java.util.ArrayList;

/**
 * Handles all the cart items in the cart list
 */
public class CartItemAdapter  extends BaseAdapter {
    private ArrayList<CommonFood> cartList;
    private int cartCount;
    private Context context;
    private Toast mToast = null;

    public CartItemAdapter(ArrayList<CommonFood> list, Context context) {
        this.cartList = list;
        this.context = context;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public int getCartCount() {
        return cartCount;
    }

    public ArrayList<CommonFood> getCartList() {
        return cartList;
    }

    @Override
    public int getCount() {
        return cartList.size();
    }

    @Override
    public Object getItem(int pos) {
        return cartList.get(pos);
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
        TextView listItemText = view.findViewById(R.id.cart_item);
        String name = cartList.get(position).getName();
        if(!cartList.get(position).getStandName().equals("")) name += " (" + cartList.get(position)
                                                                        .getStandName() + ")";
        listItemText.setText(name);

        //Handle TextView to display one cart item price
        TextView listItemPrice = view.findViewById(R.id.cart_item_price);
        listItemPrice.setText(cartList.get(position).getPriceEuro());

        //Handle TextView to display one cart item brand
        TextView listItemBrand = view.findViewById(R.id.cart_brandName);
        listItemBrand.setText(String.valueOf(cartList.get(position).getBrandName()));

        //Handle TextView to display one cart item count
        TextView listItemCount = view.findViewById(R.id.cart_item_count);
        listItemCount.setText(String.valueOf(cartList.get(position).getCount()));

        // Handle plus and minus Buttons and add onClickListeners for one menu item
        Button plusBtn = view.findViewById(R.id.cart_plus);
        plusBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(cartCount < 25) {
                    try {
                        cartList.get(position).increaseCount();
                        cartCount++;
                        // Add the itemPrice to the total price
                        ((CartActivity) context).updatePrice(cartList.get(position).getPrice());
                        notifyDataSetChanged();

                    } catch (ArithmeticException e) {
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(v.getContext(),"No more than 10 items",
                                Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                } else {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(v.getContext(),"No more than 25 in total",
                            Toast.LENGTH_SHORT);
                    mToast.show();
                }

            }
        });

        Button minusBtn = view.findViewById(R.id.cart_minus);
        minusBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cartList.get(position).decreaseCount();
                cartCount--;
                // Add the negative of the itemPrice to the total price
                ((CartActivity) context).updatePrice(cartList.get(position).getPrice().negate());
                if(cartList.get(position).getCount() == 0) cartList.remove(cartList.get(position));
                notifyDataSetChanged();
            }
        });


        return view;
    }
}
