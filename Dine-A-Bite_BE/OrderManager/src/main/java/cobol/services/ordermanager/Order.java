package cobol.services.ordermanager;

import org.json.simple.JSONObject;
import cobol.services.ordermanager.dbmenu.Food;

import java.util.*;

public class Order {
    private static int order_amount = 1;
    private int id;
    private Map<String, Integer> full_order = new HashMap<>();
    private double lat;
    private double lon;
    private int stand_id;
    public enum status {
        PENDING,
        DECLINED,
        CONFIRMED,
        READY
    }
    private status orderStatus;
    /**
     *
     * @param order_file JSON file received from the attendee-app
     *
     * Constructs an order object from a JSON file
     */
    public Order(JSONObject order_file, MenuHandler handler) {
        this.id = order_amount;
        order_amount++;
        orderStatus = status.PENDING;
        Map<String,Double> coordinates = (Map<String,Double>)order_file.get("location");
        this.lat = (double) coordinates.get("latitude");
        this.lon = (double) coordinates.get("longitude");
        Map<String,Integer> order_data = (Map<String,Integer>) order_file.get("order");
        Iterator<String> keys = order_data.keySet().iterator();
        while (keys.hasNext()){
            String name_brandName = keys.next();
            int amount = (int)order_data.get(name_brandName);
            String[] args = name_brandName.split("_");
            full_order.put(args[0], amount);
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
        String food = "Nice pizza";
        this.full_order.put(food, 2);
    }

    public int getId() {
        return id;
    }

    public status getOrderStatus() {
        return orderStatus;
    }

    public void setState(status state) {
        this.orderStatus = state;
    }

    public void setStand_id(int id) {
        this.stand_id = id;
    }

    public int getStand_id() {
        return this.stand_id;
    }
}
