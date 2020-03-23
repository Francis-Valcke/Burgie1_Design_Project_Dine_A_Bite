package cobol.services.standmanager;

import java.util.Comparator;
import java.util.Map;

/**
 * this class is used to compare and sort Stands (so schedulers) based on their remaining queuetime
 */
public class SchedulerComparatorTime implements Comparator<Scheduler> {
    private Map<String, Integer> orders;
    public SchedulerComparatorTime(Map<String,Integer> orders){
        this.orders=orders;
    }
    @Override
    public int compare(Scheduler o1, Scheduler o2) {
        int time1=0;
        int time2=0;
        for (String key : orders.keySet()){
            time1+=o1.getPreptime(key)*orders.get(key);
            time2+=o2.getPreptime(key)*orders.get(key);
        }
        return Long.compare(o1.timeSum()+time1, o2.timeSum()+time2);
    }
    public int getTimesum(Scheduler o){
        int time=0;
        for (String key : orders.keySet()){
            time+=o.getPreptime(key)*orders.get(key);
        }
        return o.timeSum()+time;
    }
}
