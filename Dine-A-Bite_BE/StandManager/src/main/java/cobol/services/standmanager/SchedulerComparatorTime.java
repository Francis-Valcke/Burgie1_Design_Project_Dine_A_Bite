package cobol.services.standmanager;


import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class used to compare schedulers based on their remaining queue time
 */
public class SchedulerComparatorTime implements Comparator<Scheduler> {
    private ArrayList<CommonOrderItem> orderItems;

    /**
     * Class constructor
     *
     * @param orderItems ArrayList containing the items of the order
     */
    public SchedulerComparatorTime(ArrayList<CommonOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    /**
     * Compares queue times of 2 schedulers
     *
     * @param o1 scheduler 1
     * @param o2 scheduler 2
     * @return 0 if both schedulers have the same queue time
     * -1 if scheduler 1 has lowest queue time
     * 1 if scheduler 2 has lowest queue time
     */
    @Override
    public int compare(Scheduler o1, Scheduler o2) {
        int time1 = getLongestFoodPrepTime(o1);
        int time2 = getLongestFoodPrepTime(o2);

        return Long.compare(o1.timeSum() + time1, o2.timeSum() + time2);
    }

    /**
     * Calculates how long it would take for an order to be finished on this scheduler
     *
     * @param o scheduler for which we want to get this timesum from
     * @return sum of the order preraration time and the scheduler queue time
     */
    public int getTimesum(Scheduler o) {
        //we use the fooditem with longest preparation time as the general preparation time of the full order

        int longestFoodPrepTime = getLongestFoodPrepTime(o);
        return o.timeSum() + longestFoodPrepTime;
    }

    /**
     * For the preparation time of an order, we currently use the largest preparation time of all the items present in that order
     *
     * @param o scheduler for which we want the order preparation time
     * @return preparation time of the order on this specific scheduler
     */
    public int getLongestFoodPrepTime(Scheduler o) {
        int longestFoodPrepTime = 0;
        for (CommonOrderItem orderItem : orderItems) {
            if (o.getPreptime(orderItem.getFoodName()) > longestFoodPrepTime) {
                longestFoodPrepTime = o.getPreptime(orderItem.getFoodName());
            }
        }
        return longestFoodPrepTime;
    }

}
