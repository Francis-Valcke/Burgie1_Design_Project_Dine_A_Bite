package cobol.services.eventchannel;

public class Event {
    private static int idCount = 0;
    private int myId;
    protected String orderData;
    private String[] types;

    public Event() {
        myId = idCount;
        idCount++;
        orderData = "Default data";
    }


    public Event(String data, String[] types) {
        myId = idCount;
        idCount++;
        orderData = data;
        this.types = types;
    }

    public String getOrderData() {
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
