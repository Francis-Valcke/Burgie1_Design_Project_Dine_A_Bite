package com.example.standapp.order;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class Event {

    private static int idCount = 0;
    private int myId;
    private String dataType;
    private JsonNode eventData;
    private List<String> types;

    public Event() {
        myId = idCount;
        idCount++;
        eventData = null;
    }

    public Event(JsonNode data, List<String> types, String dataType) {
        myId = idCount;
        idCount++;
        eventData = data;
        this.types = types;
        this.dataType = dataType;
    }

    public JsonNode getEventData() {
        return this.eventData;
    }

    public void setEventData(JsonNode data) {
        this.eventData = data;
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

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "myId=" + myId +
                ", dataType='" + dataType + '\'' +
                ", eventData=" + eventData.toString() +
                ", types=" + types +
                '}';
    }
}
