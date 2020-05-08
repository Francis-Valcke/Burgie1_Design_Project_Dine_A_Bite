package cobol.services.systemtester.stage;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stage extends Thread{
    private double clusterLocationLongitude;
    private double clusterLocationLatitude;
    private double clustermeanLongitude;
    private double clustermeanLatitude;
    private double time;
    private Random loc = new Random();
    private ArrayList<Attendee> attendees = new ArrayList<>();
    private Logger log;
    public Stage(double eventlocationlatitude,double eventlocationlongitude,  double meanLatitude, double meanLongitude, int total, Logger log){
        this.time=0;
        this.clustermeanLatitude=meanLatitude/50000*total;
        this.clustermeanLongitude=meanLongitude/50000*total;
        clusterLocationLatitude=loc.nextGaussian()*meanLatitude+eventlocationlatitude;
        clusterLocationLongitude=loc.nextGaussian()*meanLongitude+eventlocationlongitude;
        for (int i=0;i<total;i++){

            Attendee a = new Attendee(loc.nextGaussian()*clustermeanLatitude+clusterLocationLatitude,loc.nextGaussian()*clustermeanLongitude+clusterLocationLongitude);
            attendees.add(a);
        }
    }
    public void addStand(Stand s){
        //stand location on 2xmean offset from stage
        s.setLatitude(loc.nextGaussian()*clustermeanLatitude+clusterLocationLatitude+2*clustermeanLatitude);
        s.setLongitude(loc.nextGaussian()*clustermeanLongitude+clusterLocationLongitude+2*clustermeanLongitude);
    }
    public void run(){
        while(time<120){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            for (Attendee a : attendees){
                if (a.getOrderTime()<time&&a.getOrderid()==0){
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
    public ArrayList<Attendee> getAttendees(){
        return attendees;
    }
}
