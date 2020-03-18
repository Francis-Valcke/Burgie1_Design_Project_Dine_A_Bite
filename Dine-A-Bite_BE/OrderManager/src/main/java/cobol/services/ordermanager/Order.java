package cobol.services.ordermanager;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Order {
    private int remTime;
    private static int order_amount = 1;
    public int id;
    private Map<Food, Integer> full_order = new HashMap<>();
    private double lat;
    private double lon;


    public Map<Food, Integer> getFull_order(){
        return this.full_order;
    }
    // TODO: these 2 functions only temporary for remaining time (think this should be through event channel)
    public int getRemtime() {
        return this.remTime;
    }

    public void setRemtime(int i) {
        this.remTime = i;
    }

    public enum status {
        PENDING,
        CONFIRMED,
        READY
    }
    public status orderStatus;

    /**
     *
     * @param order_file JSON file received from the attendee-app
     * TODO: ZEER BELANGRIJK, momenteel bij de keys (en bij new food) gewoon vaste prijs en preptime, dit moet uiteraard nog aangepast worden
     * Constructs an order object from a JSON file
     */
    public Order(JSONObject order_file) {
        this.id = order_amount;
        order_amount++;
        orderStatus = status.PENDING;
        JSONObject coords = (JSONObject) order_file.get("location");
        this.lat = (double) coords.get("latitude");
        this.lon = (double) coords.get("longitude");
        JSONObject order_data = (JSONObject) order_file.get("order");
        Iterator<String> keys = order_data.keySet().iterator();
        while (keys.hasNext()){
            String key = keys.next();
            int amount = (int)order_data.get(key);

            Food item = new Food(key,2,2);
            full_order.put(item, amount);
        }
    }

    /**
     * Constructor for test purposes
     */
    public Order() {
        this.id = order_amount;
        order_amount++;
        orderStatus = status.PENDING;
        this.lat = 37.421998;
        this.lon = -122.084;
        Food food = new Food("friet", 3, 5);
        this.full_order.put(food, 2);
    }

    public void setState(status state) {
        this.orderStatus = state;
    }
}
