package com.example.standapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHashMap;
    private ArrayList<Event> listEvents;
    private ArrayList<CommonOrder> listOrders;

    ExpandableListAdapter(List<String> listDataHeader, HashMap<String, List<String>> listHashMap,
                          ArrayList<Event> listEvents, ArrayList<CommonOrder> listOrders) {
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
        this.listEvents = listEvents;
        this.listOrders = listOrders;
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(listHashMap.get(listDataHeader.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(listHashMap.get(listDataHeader.get(groupPosition)))
                .get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_expandable_list_group, parent, false);
        }

        String headerTitle = (String) getGroup(groupPosition);
        TextView listHeader = view.findViewById(R.id.order_number);
        listHeader.setText(headerTitle);

        final int finalGroupPosition = groupPosition;
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.status_toggle_button);
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                group.setSelectionRequired(true);
                if (checkedId == R.id.button_start && isChecked) {
                    Toast.makeText(group.getContext(), "Start", Toast.LENGTH_SHORT).show();
                    sendOrderStatusUpdate(finalGroupPosition, checkedId);
                } else if (checkedId == R.id.button_done && isChecked) {
                    Toast.makeText(group.getContext(), "Done", Toast.LENGTH_SHORT).show();
                    sendOrderStatusUpdate(finalGroupPosition, checkedId);
                } else if (checkedId == R.id.button_picked_up && isChecked) {
                    // TODO delete picked up, or put in other list (picked up orders list) ?
                    // TODO that can be shown in a history view for example ?
                    // TODO sent this change to server ?
                    // TODO Remove order when picked up
                }
            }
        });

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_expandable_list_item, parent, false);
        }

        final String childText = (String) getChild(groupPosition, childPosition);
        TextView textListHeader = view.findViewById(R.id.list_item);
        textListHeader.setText(childText);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void sendOrderStatusUpdate(int groupPosition, int statusId) {
        CommonOrderStatusUpdate.status newStatus = CommonOrderStatusUpdate.status.PENDING;
        if (statusId == R.id.button_start) {
            newStatus = CommonOrderStatusUpdate.status.CONFIRMED;
        } else if (statusId == R.id.button_done) {
            newStatus = CommonOrderStatusUpdate.status.READY;
        }
        CommonOrderStatusUpdate orderStatusUpdate = new CommonOrderStatusUpdate(listOrders.get(groupPosition).getId(), newStatus);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode eventData = mapper.valueToTree(orderStatusUpdate);
        Event event = new Event(eventData, listEvents.get(groupPosition).getTypes(), "OrderStatusUpdate");
        // TODO not finished
    }
}
