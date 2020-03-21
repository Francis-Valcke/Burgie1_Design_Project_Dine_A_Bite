package cobol.services.ordermanager;

import org.json.simple.JSONObject;
import cobol.services.ordermanager.dbmenu.Food;
//import cobol.services.ordermanager.Food;

import java.util.*;

public class Order {
    private int remTime;
    private static int order_amount = 1;
    private int id;
    //private Map<List<Food>, Integer> full_order = new HashMap<>();
    private Map<Food, Integer> full_order = new HashMap<>();
    private double lat;
    private double lon;

    public double getLat(){
        return this.lat;
    }

    public double getLon(){
        return this.lon;
    }


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
        DECLINED,
        CONFIRMED,
        READY
    }
    private status orderStatus;
    /**
     *
     * @param order_file JSON file received from the attendee-app
     * TODO: ZEER BELANGRIJK, momenteel bij de keys (en bij new food) gewoon vaste prijs en preptime, dit moet uiteraard nog aangepast worden
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
            List<Food> food = new ArrayList<>();
            if (args.length == 1) {
                food = handler.getCategory(args[0]);
            } else {
                food.add(handler.getFood(args[0], args[1]));
            }
            full_order.put(food, amount);
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
        //List<Food> food = new ArrayList<>();
        Food friet =  new Food("pizza", 2, 2);
        //this.full_order.put(food, 2);
        this.full_order.put(friet, 1);
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
