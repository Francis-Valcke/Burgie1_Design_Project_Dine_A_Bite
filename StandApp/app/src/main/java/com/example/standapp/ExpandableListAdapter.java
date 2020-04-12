package com.example.standapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO JSON parse error with publish event request to server

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private ArrayList<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;
    private ArrayList<Event> listEvents;
    private ArrayList<CommonOrder> listOrders;
    private HashMap<String, CommonOrderStatusUpdate.status> listStatus;

    ExpandableListAdapter(ArrayList<String> listDataHeader, HashMap<String, List<String>> listHashMap,
                          ArrayList<Event> listEvents, ArrayList<CommonOrder> listOrders,
                          HashMap<String, CommonOrderStatusUpdate.status> listStatus) {
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
        this.listEvents = listEvents;
        this.listOrders = listOrders;
        this.listStatus = listStatus;
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

        final String headerTitle = (String) getGroup(groupPosition); // equals order number
        TextView listHeader = view.findViewById(R.id.order_number);
        listHeader.setText(headerTitle);

        final View finalView = view;
        final int finalGroupPosition = groupPosition;
        CommonOrderStatusUpdate.status status = listStatus.get(headerTitle);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.status_toggle_button);
        if (status == CommonOrderStatusUpdate.status.PENDING) {
            toggleGroup.findViewById(R.id.button_start).setEnabled(true);
            toggleGroup.findViewById(R.id.button_done).setEnabled(false);
            toggleGroup.findViewById(R.id.button_picked_up).setEnabled(false);
        } else if (status == CommonOrderStatusUpdate.status.CONFIRMED) {
            toggleGroup.check(R.id.button_start);
            toggleGroup.findViewById(R.id.button_done).setEnabled(true);
            toggleGroup.findViewById(R.id.button_picked_up).setEnabled(false);
        } else if (status == CommonOrderStatusUpdate.status.READY) {
            toggleGroup.findViewById(R.id.button_start).setEnabled(false);
            toggleGroup.check(R.id.button_done);
            toggleGroup.findViewById(R.id.button_picked_up).setEnabled(true);
        } else {
            toggleGroup.findViewById(R.id.button_start).setEnabled(false);
            toggleGroup.findViewById(R.id.button_done).setEnabled(false);
            toggleGroup.check(R.id.button_picked_up);
        }
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                group.setSelectionRequired(true);
                if (checkedId == R.id.button_start && isChecked) {
                    Toast.makeText(group.getContext(), headerTitle + ": Start",
                            Toast.LENGTH_SHORT).show();
                    listStatus.put(headerTitle, CommonOrderStatusUpdate.status.CONFIRMED);
                    sendOrderStatusUpdate(finalGroupPosition, finalView.getContext());
                    group.findViewById(R.id.button_done).setEnabled(true);
                } else if (checkedId == R.id.button_done && isChecked) {
                    Toast.makeText(group.getContext(), headerTitle + ": Done",
                            Toast.LENGTH_SHORT).show();
                    listStatus.put(headerTitle, CommonOrderStatusUpdate.status.READY);
                    sendOrderStatusUpdate(finalGroupPosition, finalView.getContext());
                    group.findViewById(R.id.button_picked_up).setEnabled(true);
                } else if (checkedId == R.id.button_picked_up && isChecked) {
                    Toast.makeText(group.getContext(), headerTitle + ": Picked up",
                            Toast.LENGTH_SHORT).show();
                    listStatus.put(headerTitle, CommonOrderStatusUpdate.status.PICKED_UP);
                    // TODO delete picked up, or put in other list (picked up orders list) ?
                    // TODO that can be shown in a history view for example ?
                    // TODO sent this change to server ?
                    // TODO Remove order when picked up
                } else if (checkedId == R.id.button_start) {
                    group.findViewById(R.id.button_start).setEnabled(false);
                } else if (checkedId == R.id.button_done) {
                    group.findViewById(R.id.button_done).setEnabled(false);
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

    /**
     * Send order status update to Event Channel
     * @param groupPosition position of order information in arrays
     * @param context context of the callback
     */
    private void sendOrderStatusUpdate(int groupPosition, final Context context) {
        CommonOrderStatusUpdate.status newStatus = listStatus.get(listDataHeader.get(groupPosition));
        //CommonOrderStatusUpdate orderStatusUpdate = new CommonOrderStatusUpdate(listOrders.get(groupPosition).getId(), newStatus);
        ObjectMapper mapper = new ObjectMapper();
        //JsonNode eventData = mapper.valueToTree(orderStatusUpdate);
        JSONObject eventData = new JSONObject();
        try {
            eventData.put("orderId", listOrders.get(groupPosition).getId());
            if (newStatus != null) eventData.put("newStatus", newStatus.toString());
            else eventData.put("newStatus", "PENDING");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Event event = new Event(eventData, listEvents.get(groupPosition).getTypes(), "OrderStatusUpdate");
        JSONObject jsonObject = null;
        try {
            String jsonString = mapper.writeValueAsString(event);
            jsonObject = new JSONObject(jsonString);
        } catch (JsonProcessingException | JSONException e) {
            e.printStackTrace();
            Log.v("JsonException in order", e.toString());
            // Better exception handling needed
        }
        if (jsonObject!= null) System.out.println(jsonObject.toString());

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.EC_ADDRESS + "/publishEvent";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // There is no response (for now)
                System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Publish event error: " + error.toString());
                error.printStackTrace();
                Toast.makeText(context, "Publish event: " + error.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", ServerConfig.AUTHORIZATION_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }
}
