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

import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderItem;

import java.util.ArrayList;

/**
 * Handles all the order items in the order expandable list overview
 */
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
        // Add 2 to childCount because of header and footer
        return orders.get(i).getOrderItems().size() + 2;
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

    /**
     * Create the view for one group item in the expandable list, always visible
     * @param i = the position of the group item
     * @param b = if the group item is currently selected (and expanded)
     * @param view
     * @param viewGroup
     * @return
     */
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        CommonOrder currentOrder = orders.get(i);
        if(view == null)
        {
            LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_group_expandable,null);
        }

        // Make a spinning exclamation mark to notify user of changes (not used yet)
        ImageView highPriority = view.findViewById(R.id.order_group_priority);
        Animation rotation = AnimationUtils.loadAnimation(context, R.anim.priority_high);
        rotation.setDuration(1000);
        rotation.setRepeatCount(Animation.INFINITE);
        highPriority.startAnimation(rotation);
        highPriority.setVisibility(View.GONE);

        // Handle textViews of the order expandable title
        TextView textID = view.findViewById(R.id.order_group_title_id);
        TextView textCount = view.findViewById(R.id.order_group_title_count);
        TextView txtPrice = view.findViewById(R.id.order_group_title_price);
        textID.setText("#" + currentOrder.getId());
        textCount.setText("" + currentOrder.getTotalCount());
        txtPrice.setText(currentOrder.getTotalPriceEuro());

        // Set custom group indicator that can be in the center of the layout
        ImageView groupIndicator = view.findViewById(R.id.group_indicator);
        groupIndicator.setSelected(b);

        return view;
    }

    /**
     * Create the view of 1 child items of 1 group, only visible if group item is expanded
     * @param groupPos = the child's (parent) group item position
     * @param childPos = the child position (of all children)
     * @param b
     * @param view
     * @param viewGroup
     * @return
     */
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
        if(childPos > 0 && childPos < getChildrenCount(groupPos)-1)
        {
            // Handle one menuItem of the order being shown
            CommonOrderItem currentItem = getChild(groupPos,childPos-1);
            view = inflater.inflate(R.layout.order_item_expandable,null);

            TextView txtFoodName = view.findViewById(R.id.order_item_name);
            TextView txtCount = view.findViewById(R.id.order_item_count);
            TextView txtPrice = view.findViewById(R.id.order_item_price);
            txtFoodName.setText(currentItem.getFoodName());
            txtCount.setText("" + currentItem.getAmount());
            txtPrice.setText(currentItem.getPriceEuro());

            // Set padding for the last child
            /*if (childPos == currentOrder.getOrderItems().size()) {
                view.setPadding(0, 0, 0, 50);
            }*/
        }

        if(childPos == getChildrenCount(groupPos)-1)
        {
            view = inflater.inflate(R.layout.order_item_expandable_footer, null);
            // Handle footer for order stand and brand details
            TextView txtStandName = view.findViewById(R.id.order_footer_stand);
            TextView txtBrandName = view.findViewById(R.id.order_footer_brand);
            txtStandName.setText(currentOrder.getStandName());
            txtBrandName.setText(currentOrder.getBrandName() + ")");
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }


}
