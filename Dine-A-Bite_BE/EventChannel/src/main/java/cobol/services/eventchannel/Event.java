package cobol.services.eventchannel;

import org.json.simple.JSONObject;

public class Event {
    private static int idCount = 0;
    private int myId;
    protected JSONObject orderData;
    private String[] types;

    public Event() {
        myId = idCount;
        idCount++;
        orderData = null;
    }


    public Event(JSONObject data, String[] types) {
        myId = idCount;
        idCount++;
        orderData = data;
        this.types = types;
    }

    public JSONObject getOrderData() {
        return this.orderData;
    }

    public String[] getTypes() {
        return types;
    }

    public int getMyId() {
        return myId;
    }

    public void setTypes(String[] newTypes) {
        types = newTypes;
    }

}
