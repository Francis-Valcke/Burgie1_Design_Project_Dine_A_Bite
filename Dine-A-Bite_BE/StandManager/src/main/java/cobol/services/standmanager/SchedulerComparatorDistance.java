package cobol.services.standmanager;

import java.util.Comparator;

/**
 * this class is used to compare and sort Stands (so schedulers) based on their distance (from the place of order)
 */
public class SchedulerComparatorDistance implements Comparator<Scheduler> {
    private double lat;
    private double lon;

    /*
    constructor where the arguments are the coordinates for the order (so the place where the person is when he orders)
     */
    public SchedulerComparatorDistance(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    /**
     * compares the distance between 2 schedulers
     */
    public int compare(Scheduler o1, Scheduler o2) {
        double distance1 = getDistance(this.lat, this.lon, o1.getLat(), o1.getLon());
        double distance2 = getDistance(this.lat, this.lon, o2.getLat(), o2.getLon());
        return Double.compare(distance1, distance2);
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
