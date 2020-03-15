package cobol.services.standmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 *      - menu: the menu information of a stand: more information on menu items in class: Food
 *      - inc: a list of the incoming orders: more information on orders in class: Order
 *      - standname: a unique name
 *  TODO: change "inc" to proper schedule
 */
public class Scheduler extends Thread {
    private List<Order> inc = new ArrayList<Order>();
    private List<Food> menu =new ArrayList<Food>();
    private String standname;
    public List<Food> getMenu(){
        return menu;
    }
    public Scheduler(List<Food> types, String standname){
        this.menu=types;
        this.standname=standname;
    }

    /**
     * schedules order: add new order to the end of schedule
     */
    public void addOrder(Order o){
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
            s+=inc.get(i).getRemtime();
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
        for (int i= 0; i<menu.size();i++){
            if (menu.get(i).getType().equals(type)){
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
        if (inc.get(0).getRemtime()==0) {
            if (inc.size()==0) return;
            orderDone();
        }
        inc.get(0).setRemtime(inc.get(0).getRemtime()-1);
    }

    /**
     * calls prepClock() every second
     */
    public void run(){

            for (int i = 0; i < 60; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                prepClock();

                if (i==60){
                  i=0;
            }
        }
    }

    public String getStandname() {
        return standname;
    }
}
