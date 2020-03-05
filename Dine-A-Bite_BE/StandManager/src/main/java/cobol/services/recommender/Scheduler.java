package cobol.services.recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Scheduler extends Thread {
    private List<Order> inc = new ArrayList<Order>();
    private List<String> foodtypes =new ArrayList<String>();
    public List<String> getTypes(){
        return foodtypes;
    }
    public Scheduler(List<String> types){
        this.foodtypes=types;
    }
    public void addOrder(Order o){
        inc.add(o);
    }
    public void orderDone(){
        inc.remove(0);
        System.out.println("Order done");
    }
    public int timeSum(){
        int s=0;
        for (int i=0;i<inc.size();i++){
            s+=inc.get(i).getRemtime();
        }
        System.out.println(s);
        return s;
    }
    public boolean checkType(String type){
        if (foodtypes.contains(type)) return true;
        else return false;
    }
    public void prepClock(){
        if (inc.get(0).getRemtime()==0) {
            if (inc.size()==0) return;
            orderDone();
        }
        inc.get(0).setRemtime(inc.get(0).getRemtime()-1);
    }
    public void run(){
        for (int i = 0; i< 60; i++){
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

}
