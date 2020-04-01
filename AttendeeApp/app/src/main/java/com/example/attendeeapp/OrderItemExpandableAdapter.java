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

import com.example.attendeeapp.order.CommonOrder;
import com.example.attendeeapp.order.CommonOrderItem;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderItemExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<CommonOrder> orders;

    public OrderItemExpandableAdapter(Context context, ArrayList<CommonOrder> orders)
    {
        this.context = context;
        this.orders = orders;
    }

    @Override
    public int getGroupCount() {
        return orders.size();
    }

    @Override
    public int getChildrenCount(int i) {
        // Add 1 to childCount because of header
        return orders.get(i).getOrderItems().size() + 1;
    }

    @Override
    public CommonOrder getGroup(int i) {
        return orders.get(i);
    }

    @Override
    public CommonOrderItem getChild(int i, int i2) {
        return orders.get(i).getOrderItems().get(i2);
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
        CommonOrder currentOrder = orders.get(i);
        if(view == null)
        {
            LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_group_expandable,null);
        }

        // Make a spinning exclamation mark to notify user of changes (not used yet)
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
        textID.setText("#" + currentOrder.getId());
        textCount.setText("" + currentOrder.getTotalCount());
        txtPrice.setText(currentOrder.getTotalPriceEuro());

        // Set custom group indicator that can be in the center of the layout
        ImageView groupIndicator = (ImageView)view.findViewById(R.id.group_indicator);
        groupIndicator.setSelected(b);

        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
        CommonOrder currentOrder = getGroup(groupPos);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // The first row is used as header
        if(childPos == 0)
        {
            view = inflater.inflate(R.layout.order_item_expandable_header, null);
        }

        // Here is the ListView of the ChildView is handled
        if(childPos > 0 && childPos < getChildrenCount(groupPos))
        {
            // Handle one menuItem of the order being shown
            CommonOrderItem currentItem = getChild(groupPos,childPos-1);
            view = inflater.inflate(R.layout.order_item_expandable,null);

            TextView txtFoodName = (TextView)view.findViewById(R.id.order_item_name);
            TextView txtCount = (TextView)view.findViewById(R.id.order_item_count);
            TextView txtPrice = (TextView)view.findViewById(R.id.order_item_price);
            txtFoodName.setText(currentItem.getFoodname());
            txtCount.setText("" + currentItem.getAmount());
            txtPrice.setText(currentItem.getPriceEuro());

            // Set padding for the last child
            if (childPos == currentOrder.getOrderItems().size()) {
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
