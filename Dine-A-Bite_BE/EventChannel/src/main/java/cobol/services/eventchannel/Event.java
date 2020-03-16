package cobol.services.eventchannel;

public class Event {
    private static int idCount = 0;
    public int myId;
    protected String orderData;
    private String[] types;

    Event() {
        myId = idCount;
        idCount++;
        orderData = "Default data";
    }


    Event(String data, String[] types) {
        myId = idCount;
        idCount++;
        orderData = data;
        this.types = types;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] newTypes) {
        types = newTypes;
    }

}
