package cobol.services.systemtester.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stage {
    private double clusterLocationLongitude;
    private double clusterLocationLatitude;
    private double clustermeanLongitude;
    private double clustermeanLatitude;
    private int total;
    private Random loc = new Random();
    private ArrayList<Attendee> attendees = new ArrayList<>();
    public Stage(double eventlocationlatitude,double eventlocationlongitude,  double meanLatitude, double meanLongitude, int total){

        this.clustermeanLatitude=meanLatitude/50000*total;
        this.clustermeanLongitude=meanLongitude/50000*total;
        clusterLocationLatitude=loc.nextGaussian()*meanLatitude+eventlocationlatitude;
        clusterLocationLongitude=loc.nextGaussian()*meanLongitude+eventlocationlongitude;
        this.total=total;
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
    public ArrayList<Attendee> getAttendees(){
        return attendees;
    }
}
