package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderItem;
import com.example.attendeeapp.json.CommonOrderStatusUpdate;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Handles all the order items in the order expandable list from OrderActivity.
 */
public class OrderItemExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<CommonOrder> orders;
    private OrderDatabaseService orderDatabaseService;

    public OrderItemExpandableAdapter(Context context, ArrayList<CommonOrder> orders, OrderDatabaseService orderDatabaseService)
    {
        this.context = context;
        this.orders = orders;
        this.orderDatabaseService = orderDatabaseService;
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

    @Override
    public void onGroupExpanded(int groupPosition) {
        // Only if group expands, user has definitely seen possible order changes
        CommonOrder currentOrder = orders.get(groupPosition);
        if (!currentOrder.isUpdateSeen()) {
            currentOrder.setUpdateSeen(true);
            orderDatabaseService.updateOrder(currentOrder);
        }
        super.onGroupExpanded(groupPosition);
    }

    /**
     * Create the view for one group item in the expandable list.
     * Group items are always visible.
     *
     * @param i The position of the group item.
     * @param b If the group item is currently and expanded.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        CommonOrder currentOrder = orders.get(i);
        if (view == null)
        {
            LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // root cannot be viewGroup
            view = inflater.inflate(R.layout.order_group_expandable, null);
        }

        // Make a rotating exclamation mark to notify user of changes
        ImageView highPriority = view.findViewById(R.id.order_group_priority);
        RotateAnimation rotation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // Check if animation has to be visible (if user has seen the order update)
        if (currentOrder.isUpdateSeen()) {
            if (highPriority.getAnimation() != null) {
                highPriority.getAnimation().cancel();
                highPriority.clearAnimation();
            }
            highPriority.setVisibility(View.GONE);
        } else {
            highPriority.setVisibility(View.VISIBLE);
            rotation.setDuration(1000);
            rotation.setRepeatCount(Animation.INFINITE);
            highPriority.startAnimation(rotation);
        }

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
     * Create the view of 1 child item of 1 group.
     * Only visible if the group item is expanded.
     *
     * @param groupPos The child's (parent) group item position.
     * @param childPos The child position (in all the children).
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
        CommonOrder currentOrder = getGroup(groupPos);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // The first row is used as header
        if (childPos == 0)
        {
            view = inflater.inflate(R.layout.order_item_expandable_header, null);
        }

        // Here is the ListView of the ChildView is handled
        if (childPos > 0 && childPos < getChildrenCount(groupPos)-1)
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

        if (childPos == getChildrenCount(groupPos)-1)
        {
            view = inflater.inflate(R.layout.order_item_expandable_footer, null);
            // Handle footer for order stand and brand details
            TextView txtStandName = view.findViewById(R.id.order_footer_stand);
            TextView txtBrandName = view.findViewById(R.id.order_footer_brand);
            TextView txtOrderStatus = view.findViewById(R.id.order_footer_status);
            txtStandName.setText(currentOrder.getStandName());
            txtBrandName.setText(currentOrder.getBrandName() + ")");
            txtOrderStatus.setText(currentOrder.getOrderState().toString());

            TextView remainingTimeText = view.findViewById(R.id.order_footer_time_text);
            LinearLayout timeLayout = view.findViewById(R.id.order_footer_timer_layout);
            final TextView remainingTime = view.findViewById(R.id.order_footer_time);

            // Handle timing counter, only visible when order is confirmed and thus being prepared
            if (currentOrder.getOrderState() == CommonOrder.State.CONFIRMED) {
                remainingTimeText.setVisibility(View.VISIBLE);
                remainingTime.setVisibility(View.VISIBLE);
                timeLayout.setVisibility(View.VISIBLE);

                // Get current time instance and set countdown timer with remaining time
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));

                currentOrder.setStartTime(zonedDateTime);

                // Update database is currently not necessary because a new time instance is used as reference
                // Update database that startTime is consistent when app would be closed
                //orderDatabaseService.updateOrder(currentOrder);

                new CountDownTimer(currentOrder.computeRemainingTime(), 1000) {
                    public void onTick(long millisUntilFinished) {
                        remainingTime.setText(String.format(Locale.getDefault(), "%d m : %02d s",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60));
                    }
                    public void onFinish() {
                        remainingTime.setText("0 m : 00 s");
                    }

                }.start();

            } else {
                timeLayout.setVisibility(View.GONE);
                remainingTimeText.setVisibility(View.GONE);
                remainingTime.setVisibility(View.GONE);
            }
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    /**
     * Updates the order status for a given order id.
     * If the state is confirmed, initialize the expected order time and start the counting.
     *
     * @param orderId The order id for which the status update holds.
     * @param State The new order status.
     */
    public void updateOrder(int orderId, CommonOrderStatusUpdate.State State) {
            for (CommonOrder order : orders) {
                if (order.getId() == orderId) {
                    CommonOrder.State oldState = order.getOrderState();
                    CommonOrder.State newState = CommonOrder.State.values()[State.ordinal()];
                    if (oldState != newState) {
                        order.setOrderState(newState);
                        order.setUpdateSeen(false);

                        // Set expected time with current local time + preparing time
                        if (newState.equals(CommonOrder.State.CONFIRMED)) {
                            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
                            long timestamp = order.computeRemainingTime();
                            timestamp += now.toInstant().toEpochMilli();
                            ZonedDateTime expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Europe/Brussels"));

                            order.setExpectedTime(expectedTime);
                        }

                        try{
                            orderDatabaseService.updateOrder(order);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                            Toast.makeText(context, "Error while updating an order in local db", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
    }


}
