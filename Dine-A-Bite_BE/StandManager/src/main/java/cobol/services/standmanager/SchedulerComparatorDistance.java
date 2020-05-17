package cobol.services.standmanager;

import java.util.Comparator;


/**
 * Class used to compare schedulers based on distance to the attendee who requested the recommendation/order
 */
public class SchedulerComparatorDistance implements Comparator<Scheduler> {
    private double lat;
    private double lon;

    /**
     * Class constructor
     *
     * @param lat latitiude coordinates of location attendee
     * @param lon longitude coordinates of location attendee
     */
    public SchedulerComparatorDistance(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Compares distances to attendee for 2 different schedulers
     *
     * @param o1 scheduler 1
     * @param o2 scheduler 2
     * @return 0 if both schedulers have same distance to attendee
     * -1 if scheduler 1 is closer to attendee
     * 1 if scheduler 2 is closer to attendee
     */
    @Override
    public int compare(Scheduler o1, Scheduler o2) {
        SchedulerComparator s = new SchedulerComparator();
        double distance1 = getDistance(o1.getLat(), o1.getLon());
        double distance2 = getDistance(o2.getLat(), o2.getLon());
        return Double.compare(distance1, distance2);
    }

    /**
     * Calculate distance from the attendee to another locaion
     *
     * @param lat1 lat coords for first point
     * @param lon1 lon coords for first point
     * @return the distance between the 2 points (in meters)
     */
    public double getDistance(double lat1, double lon1) {
        double Radius = (6371 * Math.pow(10, 3)); //earth's radius in meters
        double lat1rad = (lat * (Math.PI / 180)); //lat1 in radials
        double lat2rad = (lat1 * (Math.PI / 180)); //lat2 in radials
        double latdiffrad = ((lat1 - lat) * (Math.PI / 180)); //difference between lats in radials
        double londiffrad = ((lon1 - lon) * (Math.PI / 180)); //difference between lons in radials

        double a = (Math.pow(Math.sin(latdiffrad / 2), 2) + (Math.cos(lat1rad) * Math.cos(lat2rad) * Math.pow(Math.sin(londiffrad / 2), 2)));
        double c = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        double distance = Radius * c;
        return distance;
    }


}
