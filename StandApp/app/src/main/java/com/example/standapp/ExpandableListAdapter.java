package com.example.standapp;

import android.content.Context;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.order.CommonOrder;
import com.example.standapp.order.CommonOrderStatusUpdate;
import com.example.standapp.order.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private ArrayList<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;
    private ArrayList<CommonOrder> listOrders;
    private HashMap<String, CommonOrderStatusUpdate.State> listStatus;

    private ArrayList<String> oldListDataHeader;
    private ArrayList<CommonOrder> oldListOrders;
    private ArrayList<String> activeListDataHeader;
    private ArrayList<CommonOrder> activeListOrders;

    private String standName;
    private String brandName;


    ExpandableListAdapter(ArrayList<String> listDataHeader,
                          HashMap<String, List<String>> listHashMap,
                          ArrayList<CommonOrder> listOrders,
                          HashMap<String, CommonOrderStatusUpdate.State> listStatus,
                          ArrayList<String> oldListDataHeader,
                          ArrayList<CommonOrder> oldListOrders,
                          ArrayList<String> activeListDataHeader,
                          ArrayList<CommonOrder> activeListOrders,
                          String standName,
                          String brandName) {

        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
        this.listOrders = listOrders;
        this.listStatus = listStatus;
        this.oldListDataHeader = oldListDataHeader;
        this.oldListOrders = oldListOrders;
        this.activeListDataHeader = activeListDataHeader;
        this.activeListOrders = activeListOrders;
        this.standName = standName;
        this.brandName = brandName;
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_expandable_list_group, parent, false);

        // Set order number (title/header)
        final String headerTitle = (String) getGroup(groupPosition); // equals order number
        TextView listHeader = view.findViewById(R.id.order_number);
        listHeader.setText(headerTitle);

        final View finalView = view;

        CommonOrderStatusUpdate.State State = listStatus.get(headerTitle);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.status_toggle_button);
        toggleGroup.clearChecked();

        // Set checked status in toggle group
        // - PENDING -> NOTHING
        // - CONFIRMED -> START button
        // - READY -> DONE button
        // - Others -> PICKED UP button
        if (State == CommonOrderStatusUpdate.State.PENDING) {
            toggleGroup.findViewById(R.id.button_start).setEnabled(true);
            toggleGroup.findViewById(R.id.button_done).setEnabled(false);
            toggleGroup.findViewById(R.id.button_picked_up).setEnabled(false);
        } else if (State == CommonOrderStatusUpdate.State.CONFIRMED) {
            toggleGroup.check(R.id.button_start);
            toggleGroup.findViewById(R.id.button_done).setEnabled(true);
            toggleGroup.findViewById(R.id.button_picked_up).setEnabled(false);
        } else if (State == CommonOrderStatusUpdate.State.READY) {
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

                if (checkedId == R.id.button_start && isChecked) {
                    // User clicks on START button (and checks it)
                    // - enable DONE button

                    if (listStatus.get(headerTitle) == CommonOrderStatusUpdate.State.PENDING) {
                        // The current state is PENDING (not START/CONFIRMED)
                        // - change state to START/CONFIRMED

                        Toast.makeText(group.getContext(), headerTitle + ": Start",
                                Toast.LENGTH_SHORT).show();
                        listStatus.put(headerTitle, CommonOrderStatusUpdate.State.CONFIRMED);
                        sendOrderStatusUpdate(groupPosition, finalView.getContext());

                    }

                    group.findViewById(R.id.button_done).setEnabled(true);

                } else if (checkedId == R.id.button_done && isChecked) {
                    // User clicks on DONE button (and checks it)
                    // - enable PICKED UP button

                    group.findViewById(R.id.button_picked_up).setEnabled(true);

                } else if (checkedId == R.id.button_picked_up && isChecked) {
                    // User clicks on PICKED UP button (and checks it)

                    group.check(R.id.button_picked_up);

                } else if (checkedId == R.id.button_start)  {
                    // Is activated when changing from START button

                    if (group.getCheckedButtonId() == R.id.button_done) {
                        // User clicks on DONE button
                        // - Set state to DONE and send event

                        Toast.makeText(group.getContext(), headerTitle + ": Done",
                                Toast.LENGTH_SHORT).show();
                        group.findViewById(R.id.button_start).setEnabled(false);
                        listStatus.put(headerTitle, CommonOrderStatusUpdate.State.READY);
                        sendOrderStatusUpdate(groupPosition, finalView.getContext());

                    } else {
                        // User clicks on START button
                        group.check(R.id.button_start);
                    }

                } else if (checkedId == R.id.button_done) {
                    // Is activated when changing from DONE button

                    if (group.getCheckedButtonId() == R.id.button_picked_up) {
                        // User clicks on PICKED UP button
                        // - Set state to PICKED UP and send event

                        Toast.makeText(group.getContext(), headerTitle + ": Picked up",
                                Toast.LENGTH_SHORT).show();
                        group.findViewById(R.id.button_done).setEnabled(false);
                        listStatus.put(headerTitle, CommonOrderStatusUpdate.State.PICKED_UP);
                        sendOrderStatusUpdate(groupPosition, finalView.getContext());

                        // Add to picked up orders list
                        oldListOrders.add(0, listOrders.get(groupPosition));
                        oldListDataHeader.add(0, listDataHeader.get(groupPosition));

                        // Remove from active orders list
                        activeListOrders.remove(groupPosition);
                        activeListDataHeader.remove(groupPosition);
                        notifyDataSetChanged();

                    } else {
                        // User clicks on DONE button
                        group.check(R.id.button_done);
                    }

                }
                else if (checkedId == R.id.button_picked_up) {
                    // Is activated when changing from PICKED UP button

                    group.check(R.id.button_picked_up);

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

    /**
     * Send order status update to Event Channel
     * @param groupPosition position of order information in arrays
     * @param context context of the callback
     */
    private void sendOrderStatusUpdate(int groupPosition, final Context context) {

        final LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        CommonOrderStatusUpdate.State newState = listStatus.get(listDataHeader.get(groupPosition));
        ObjectMapper mapper = new ObjectMapper();

        CommonOrderStatusUpdate orderStatusUpdate
                = new CommonOrderStatusUpdate(listOrders.get(groupPosition).getId(), newState);
        JsonNode eventData = mapper.valueToTree(orderStatusUpdate);

        ArrayList<String> types = new ArrayList<>();
        types.add("s_" + standName + "_" + brandName);
        types.add("o_" + listOrders.get(groupPosition).getId());

        Event event = new Event(eventData, types, "OrderStatusUpdate");
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Send order status update: " + jsonString);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ServerConfig.EC_ADDRESS + "/publishEvent";

        final String finalJsonString = jsonString;
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // There is no response (for now)
                System.out.println(response);
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
            public byte[] getBody() {
                return finalJsonString.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        queue.add(request);
    }

    void setStandName(String standName) {
        this.standName = standName;
    }

    void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    void setListDataHeader(ArrayList<String> listDataHeader) {
        this.listDataHeader = listDataHeader;
    }

    void setListOrders(ArrayList<CommonOrder> listOrders) {
        this.listOrders = listOrders;
    }
}
