package cobol.services.standmanager;

import cobol.commons.order.CommonOrderItem;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * class for weighted sorting of schedulers
 */
public class SchedulerComparator implements Comparator<Scheduler> {
    private double lat;
    private double lon;
    private double weight;
    private ArrayList<CommonOrderItem> orderItems;
    private SchedulerComparatorTime schedulerTime;
    private SchedulerComparatorDistance schedulerDistance;

    /*
    constructor where the arguments are the coordinates for the order (so the place where the person is when he orders) and the weight of how much distance is in time (for combination of time and distance scheduling)
     */
    public SchedulerComparator(double lat, double lon, double weight, ArrayList<CommonOrderItem> orderItems) {
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
        this.orderItems = orderItems;
        schedulerTime = new SchedulerComparatorTime(orderItems);
        schedulerDistance = new SchedulerComparatorDistance(lat, lon);
    }

    public SchedulerComparator() {

    }

    @Override
    /**
     * compares the time between 2 schedulers where there is time added as distance*weight
     */
    public int compare(Scheduler o1, Scheduler o2) {
        double time1 = schedulerTime.getLongestFoodPrepTime(o1);
        double time2 = schedulerTime.getLongestFoodPrepTime(o2);
        double distance1 = schedulerDistance.getDistance(o1.getLat(), o1.getLon());
        double distance2 = schedulerDistance.getDistance(o2.getLat(), o2.getLon());
        double extra1 = distance1 * weight;
        double extra2 = distance2 * weight;
        return Double.compare(o1.timeSum() + time1 + extra1, o2.timeSum() + time2 + extra2);
    }
}
