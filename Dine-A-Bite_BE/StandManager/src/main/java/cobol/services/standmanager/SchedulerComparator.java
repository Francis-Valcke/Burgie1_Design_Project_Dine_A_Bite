package cobol.services.standmanager;

import cobol.commons.order.CommonOrderItem;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class used to compare schedulers based on time and the distance to the attendee who requested the recommendation/order
 */
public class SchedulerComparator implements Comparator<Scheduler> {
    private double lat;
    private double lon;
    private double weight;
    private ArrayList<CommonOrderItem> orderItems;
    private SchedulerComparatorTime schedulerTime;
    private SchedulerComparatorDistance schedulerDistance;

    /**
     * Class constructor
     *
     * @param lat        latitiude coordinates of location attendee
     * @param lon        longitude coordinates of location attendee
     * @param weight     weight used to convert distance to time, distance X weight will be the added time
     * @param orderItems list of all the orderitems for that order
     */
    public SchedulerComparator(double lat, double lon, double weight, ArrayList<CommonOrderItem> orderItems) {
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
        this.orderItems = orderItems;
        schedulerTime = new SchedulerComparatorTime(orderItems);
        schedulerDistance = new SchedulerComparatorDistance(lat, lon);
    }

    /**
     * Alternative class constructor, used to acces functions in other classes
     */
    public SchedulerComparator() {

    }

    /**
     * Compares queue time + distance (to order) X weight for 2 different schedulers
     *
     * @param o1 scheduler 1
     * @param o2 scheduler 2
     * @return 0 if both schedulers have the same queue time (+ weighted distance)
     * -1 if scheduler 1 has lowest queue time (+ weighted distance)
     * 1 if scheduler 2 has lowest queue time (+ weighted distance)
     */
    @Override
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
