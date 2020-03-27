package cobol.services.standmanager;

import cobol.commons.order.CommonOrder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 *  TODO: change "inc" to proper schedule
 */
public class Scheduler extends Thread {
    // a list of incoming orders
    private List<CommonOrder> inc = new LinkedList<>();

    // FoodName, Array [prepTime, Price] -- MenuItem
    private Map<String, int[]> menu =new HashMap<>();

    // Stand identifiers
    private String standname;
    private int id;

    // Coordinates of Stand
    private double lon;
    private double lat;

    private String brand;


    public Scheduler(Map<String, int[]> menu, String standname, int id, String brand){
        this.menu=menu;
        this.standname=standname;
        this.id = id;
        this.brand = brand;
    }

    /**
     * schedules order: add new order to the end of schedule
     */
    public void addOrder(CommonOrder o){
        inc.add(o);
    }

    /**
     * removes first order from schedule
     */
    public void orderDone(){
        inc.remove(0);
        System.out.println("Order done");
    }

    /**
     * calculates total time to end of schedule
     * @return this time
     */
    public int timeSum(){
        int s=0;
        for (int i=0;i<inc.size();i++){
            s+=inc.get(i).getRemainingTime();
        }
        System.out.println(s);
        return s;
    }

    /**
     * checks if a food item is present in the stand menu
     * @param type: requested food item
     * @return true/false
     */
    public boolean checkType(String type){
        for (String food : menu.keySet()){
            if (food.equals(type)){
                return true;
            }
        }
        return false;
    }

    /**
     * Removes 1 (second) from the remaining time of the first scheduled order: the order that should be under preparation
     * TODO: remove 1 (second) from all orders that are flagged as "under preparation" (+ add flag for "preparation")
        */
    public void prepClock(){
        if (inc.size() == 0){
            return;
        }
        else {
            if (inc.get(0).getRemainingTime() < 0) {
                if (inc.size() == 0) return;
                orderDone();
            }
        }
    }

    public void run(){
        while (true){
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            prepClock();
        }
    }


    public int getPreptime(String foodname){
        return menu.get(foodname)[1];
    }
    public double getLon(){
        return this.lon;
    }

    public double getLat(){
        return this.lat;
    }

    public void setLon(double l){
        this.lon = l;
    }

    public void setLat(double l){
        this.lat = l;
    }

    public Map<String, int[]> getMenu(){
        return this.menu;
    }

    public int getStandId(){
        return this.id;
    }

    public String getStandName(){
        return this.standname;
    }

    public String getBrand(){
        return this.brand;
    }

}
