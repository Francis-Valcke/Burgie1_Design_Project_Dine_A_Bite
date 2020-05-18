package cobol.services.systemtester.stage;

import cobol.commons.order.CommonOrder;
import cobol.services.systemtester.ServerConfig;
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
    private boolean systemOn;
    private Logger log;

    public Stage(double eventlocationlatitude, double eventlocationlongitude, double meanLatitude, double meanLongitude, int total, Logger log) {
        this.time = 0;
        this.clustermeanLatitude = meanLatitude / 50000 * total;
        this.clustermeanLongitude = meanLongitude / 50000 * total;
        clusterLocationLatitude = loc.nextGaussian() * meanLatitude + eventlocationlatitude;
        clusterLocationLongitude = loc.nextGaussian() * meanLongitude + eventlocationlongitude;
        this.log=log;
        systemOn=true;
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
    public void chooseOrders(){
        for (Attendee a : attendees) {
            a.getGlobalMenu().subscribe(
                    items -> a.chooseOrder(items, 1),
                    throwable -> log.error(throwable.getMessage())
            );
        }
    }
    public void systemRun(){
        while (time < ServerConfig.totaltestseconds) {
            try {
                Thread.sleep(1000);//1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            for (Attendee a : attendees) {
                if (Math.ceil(a.getOrderTime()) == time) {
                    a.placeOrder(CommonOrder.RecommendType.DISTANCE_AND_TIME).subscribe(
                            recommendations -> a.confirmStand().subscribe(),
                            throwable -> log.error(throwable.getMessage())

                    );
                }
            }
        }
    }
    public void noSystemRun(){
        while (time < ServerConfig.totaltestseconds*3) {
            try {
                Thread.sleep(1000);//1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            for (Attendee a : attendees) {
                if (Math.ceil(a.getOrderTime()) == time) {
                    if (a.getWalking()){
                        a.placeOrder(CommonOrder.RecommendType.DISTANCE).subscribe(
                                recommendations -> a.confirmStand().subscribe(),
                                throwable -> log.error(throwable.getMessage())

                        );
                    }
                    else{
                        a.orderForNearestStand().subscribe(
                                recommendations -> {
                                    log.info("Attendee" + a.getId()+ " starts walking to stand");
                                    a.getRecommendedStand();
                                    a.setNewOrdertime(a.getOrderTime()+a.getWalkingStartTime());
                                },
                                throwable -> log.error(throwable.getMessage())

                        );
                    }
                }
            }
        }
    }
    public void run() {
        if (systemOn)systemRun();
        else noSystemRun();
    }

    public ArrayList<Attendee> getAttendees() {
        return attendees;
    }
    public void setSystemOn(boolean SystemOn){
        this.systemOn=systemOn;
    }
}
