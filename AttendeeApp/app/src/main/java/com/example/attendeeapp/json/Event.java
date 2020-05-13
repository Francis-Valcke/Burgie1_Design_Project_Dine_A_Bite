package com.example.attendeeapp.json;
import java.util.List;

import org.json.JSONObject;

public class Event {
    private static int idCount = 0;
    private int myId;
    private String dataType;
    protected JSONObject eventData;
    private List<String> types;

    public Event() {
        myId = idCount;
        idCount++;
        eventData = null;
    }


    public Event(JSONObject data, List<String> types, String dataType) {
        myId = idCount;
        idCount++;
        eventData = data;
        this.types = types;
        this.dataType = dataType;
    }

    public JSONObject getEventData() {
        return this.eventData;
    }

    public List<String> getTypes() {
        return types;
    }

    public int getMyId() {
        return myId;
    }

    public void setTypes(List<String> newTypes) {
        types = newTypes;
    }

    public String getDataType() {
        return dataType;
    }

}
