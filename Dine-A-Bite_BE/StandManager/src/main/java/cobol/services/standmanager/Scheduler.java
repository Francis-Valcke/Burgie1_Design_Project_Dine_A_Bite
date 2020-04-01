package cobol.services.standmanager;

import cobol.commons.order.CommonOrder;
import cobol.commons.CommonFood;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 *  TODO: change "inc" to proper schedule
 */
public class Scheduler extends Thread {
    private List<CommonOrder> inc = new LinkedList<>();
    private List<CommonFood> menu;
    private String standName;

    // Coordinates of Stand
    private double lon;
    private double lat;

    private String brandName;

    public Scheduler(List<CommonFood> menu, String standName, String brandName, double lat, double lon) {
        this.menu = menu;
        this.standName = standName;
        this.brandName = brandName;
        this.lat= lat;
        this.lon=lon;
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

    public List<CommonFood> getMenu(){
        return this.menu;
    }

    public String getStandName(){
        return this.standName;
    }

    public String getBrandName(){
        return this.brandName;
    }

    /**
     * gives preptime of item in scheduler
     * @param foodname name of item
     * @return preptime
     */
    public int getPreptime(String foodname) {
        for (CommonFood m : menu) {
            if (m.getName().equals(foodname)) return m.getPreparationTime();
        }
        return -1;
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
    public void orderDone() {
        inc.remove(0);
        System.out.println("Order done");
    }

    /**
     * calculates total time to end of schedule
     *
     * @return this time
     */
    public int timeSum(){
        int s=0;
        for (int i=0;i<inc.size();i++){
            s+=inc.get(i).computeRemainingTime();
        }
        System.out.println(s);
        return s;
    }

    /**
     * checks if a food item is present in the stand menu
     *
     * @param type: requested food item
     * @return true/false
     */
    public boolean checkType(String type) {
        for (CommonFood mi : menu) {
            if (mi.getName().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes 1 (second) from the remaining time of the first scheduled order: the order that should be under preparation
     * TODO: remove 1 (second) from all orders that are flagged as "under preparation" (+ add flag for "preparation")
     */
    public void prepClock() {
        if (inc.size() == 0) {
            return;
        }
        else {
            if (inc.get(0).computeRemainingTime() < 0) {
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



}
