package cobol.services.systemtester.stage;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Random;

public class Stage extends Thread {
    private final double clusterLocationLongitude;
    private final double clusterLocationLatitude;
    private final double clustermeanLongitude;
    private final double clustermeanLatitude;
    private double time;
    private final Random loc = new Random();
    private final ArrayList<Attendee> attendees = new ArrayList<>();
    private Logger log;

    public Stage(double eventlocationlatitude, double eventlocationlongitude, double meanLatitude, double meanLongitude, int total, Logger log) {
        this.time = 0;
        this.clustermeanLatitude = meanLatitude / 50000 * total;
        this.clustermeanLongitude = meanLongitude / 50000 * total;
        clusterLocationLatitude = loc.nextGaussian() * meanLatitude + eventlocationlatitude;
        clusterLocationLongitude = loc.nextGaussian() * meanLongitude + eventlocationlongitude;
        for (int i = 0; i < total; i++) {

            Attendee a = new Attendee(loc.nextGaussian() * clustermeanLatitude + clusterLocationLatitude, loc.nextGaussian() * clustermeanLongitude + clusterLocationLongitude);
            attendees.add(a);
        }
    }

    public void addStand(Stand s) {
        //stand location on 2xmean offset from stage
        s.setLatitude(loc.nextGaussian() * clustermeanLatitude + clusterLocationLatitude + 2 * clustermeanLatitude);
        s.setLongitude(loc.nextGaussian() * clustermeanLongitude + clusterLocationLongitude + 2 * clustermeanLongitude);
    }

    public void run() {
        while (time < 120) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            for (Attendee a : attendees) {
                if (Math.ceil(a.getOrderTime()) == time) {
                    a.getGlobalMenu().subscribe(
                            items -> a.placeRandomOrder(items, 1).subscribe(
                                    recommendations -> a.confirmNearestStand().subscribe(),
                                    throwable -> log.error(throwable.getMessage())
                            )
                    );

                }
            }
        }
    }

    public ArrayList<Attendee> getAttendees() {
        return attendees;
    }
}
