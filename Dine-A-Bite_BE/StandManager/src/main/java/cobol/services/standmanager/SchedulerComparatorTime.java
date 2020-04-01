package cobol.services.standmanager;


import cobol.commons.order.CommonOrderItem;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * this class is used to compare and sort Stands (so schedulers) based on their remaining queuetime
 */
public class SchedulerComparatorTime implements Comparator<Scheduler> {
    private ArrayList<CommonOrderItem> orderItems;
    public SchedulerComparatorTime(ArrayList<CommonOrderItem> orderItems){
        this.orderItems=orderItems;
    }


    @Override
    public int compare(Scheduler o1, Scheduler o2) {
        int time1=0;
        int time2=0;
        for (CommonOrderItem orderItem : orderItems) {
            String foodName= orderItem.getFoodName();
            time1+=o1.getPreptime(foodName)*orderItem.getAmount();
            time2+=o2.getPreptime(foodName)*orderItem.getAmount();
        }
        return Long.compare(o1.timeSum() + time1, o2.timeSum() + time2);
    }


    public int getTimesum(Scheduler o){
        int time=0;
        for (CommonOrderItem orderItem : orderItems) {
            time+=o.getPreptime(orderItem.getFoodName())*orderItem.getAmount();
        }
        return o.timeSum() + time;
    }
}
