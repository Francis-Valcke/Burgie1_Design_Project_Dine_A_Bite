package com.example.attendeeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderExpandableItemAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<ArrayList<MenuItem>> orders;
    private BigDecimal totalPrice;
    private int orderCount;
    private int orderId;

    public OrderExpandableItemAdapter(Context context, ArrayList<ArrayList<MenuItem>> orders)
    {
        this.context = context;
        this.orders = orders;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public int getGroupCount() {
        return orders.size();
    }

    @Override
    public int getChildrenCount(int i) {
        // Add 1 to childCount because of header
        return orders.get(i).size() + 1;
    }

    @Override
    public ArrayList<MenuItem> getGroup(int i) {
        return orders.get(i);
    }

    @Override
    public MenuItem getChild(int i, int i2) {
        return orders.get(i).get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        ArrayList<MenuItem> currentOrder = orders.get(i);
        if(view == null)
        {
            LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_group_expandable,null);
        }

        ImageView highPriority = (ImageView)view.findViewById(R.id.order_group_priority);
        Animation rotation = AnimationUtils.loadAnimation(context, R.anim.priority_high);
        rotation.setDuration(1000);
        rotation.setRepeatCount(Animation.INFINITE);
        highPriority.startAnimation(rotation);
        highPriority.setVisibility(View.GONE);

        // Handle textViews of the order expandable title
        TextView textID = (TextView)view.findViewById(R.id.order_group_header_id);
        TextView textCount = (TextView)view.findViewById(R.id.order_group_header_count);
        TextView txtPrice = (TextView)view.findViewById(R.id.order_group_header_price);
        textID.setText("" + orderId);
        textCount.setText("" + orderCount);

        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        euro.setMinimumFractionDigits(2);
        String symbol = euro.getCurrency().getSymbol();
        txtPrice.setText(symbol + " " + totalPrice);

        // Set custom group indicator that can be in the center of the layout
        ImageView groupIndicator = (ImageView)view.findViewById(R.id.group_indicator);
        groupIndicator.setSelected(b);

        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
        ArrayList<MenuItem> currentOrder = getGroup(groupPos);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // The first row is used as header
        if(childPos == 0)
        {
            view = inflater.inflate(R.layout.order_item_expandable_header, null);
        }

        // Here is the ListView of the ChildView
        if(childPos > 0 && childPos < getChildrenCount(groupPos))
        {
            // Handle one menuItem of the order being shown
            MenuItem currentItem = getChild(groupPos,childPos-1);
            view = inflater.inflate(R.layout.order_item_expandable,null);

            TextView txtFoodName = (TextView)view.findViewById(R.id.order_item_name);
            TextView txtCount = (TextView)view.findViewById(R.id.order_item_count);
            TextView txtPrice = (TextView)view.findViewById(R.id.order_item_price);
            txtFoodName.setText(currentItem.getFoodName());
            txtCount.setText("" + currentItem.getCount());
            txtPrice.setText(currentItem.getPriceEuro());

            // Set padding for the last child
            if (childPos == currentOrder.size()) {
                view.setPadding(0, 0, 0, 50);
            }
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }


}
