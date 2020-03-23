package cobol.services.standmanager;

import java.util.Comparator;

/**
 * class for weighted sorting of schedulers
 */
public class SchedulerComparator implements Comparator<Scheduler> {
    private double lat;
    private double lon;
    private double weight;

    /*
    constructor where the arguments are the coordinates for the order (so the place where the person is when he orders) and the weight of how much distance is in time (for combination of time and distance scheduling)
     */
    public SchedulerComparator(double lat, double lon, double weight){
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
    }

    public SchedulerComparator(){

    }

    @Override
    /**
     * compares the time between 2 schedulers where there is time added as distance*weight
     */
    public int compare(Scheduler o1, Scheduler o2) {
        double distance1 = getDistance(this.lat, this.lon, o1.getLat(), o1.getLon());
        double distance2 = getDistance(this.lat, this.lon, o2.getLat(), o2.getLon());
        double extra1 = distance1*weight;
        double extra2 = distance2*weight;
        return Double.compare(o1.timeSum() + extra1, o2.timeSum() + extra2);
    }

    /**
     *
     * @param lat1 lat coords for first point
     * @param lon1 lon coords for first point
     * @param lat2 lat coords for second point
     * @param lon2 lon coords for second point
     * @return the distance between the 2 points
     */
    public double getDistance(double lat1, double lon1, double lat2, double lon2){
        double xdiff = Math.pow(lat2 - lat1,2);
        double ydiff = Math.pow(lon2 - lon1,2);
        double distance = Math.sqrt(xdiff + ydiff);
        return distance;
    }
}
