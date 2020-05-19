package cobol.services.systemtester;

import cobol.commons.order.CommonOrder;
import cobol.services.systemtester.stage.Attendee;
import cobol.services.systemtester.stage.Stage;
import cobol.services.systemtester.stage.Stand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventSimulation {
    private static final Logger log = LogManager.getLogger(EventSimulation.class);
    private ArrayList<Stand> stands = new ArrayList<>();
    private final ArrayList<Stage> stages = new ArrayList<Stage>();

    /**
     * This constructor will initialize event with stands from dataset.json
     * @throws IOException
     */
    public EventSimulation() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("dataset.json");
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        stands = (ArrayList<Stand>) mapper.readValue(body, new TypeReference<List<Stand>>() {
        });

    }

    /**
     * This method will setup event with 5 stages, locations and ordertimes are chosen with gaussian around centerpoint and means are chosen in regards to real events
     * @param size number of attendees
     */
    public void setup(int size)  {
        ModuleHandler mh = new ModuleHandler();
        mh.allAlive();
        Random ran = new Random();
        int j = 0;
        //create stages and spread stands around stages
        int stageCount = 5;
        for (int i = 0; i < stageCount; i++) {
            // Each degree of latitude is approximately 111 kilometers apart
            // At 40 degrees north or south, the distance between a degree of longitude is 85 kilometers
            // distribute the stages around event with a mean of approximately 1 km
            Stage s = new Stage(ServerConfig.latStart, ServerConfig.lonStart, 1.0 / 111, 1.0 / 85, size / stageCount, log);
            if (j < stands.size()) s.addStand(stands.get(j));
            j++;
            if (j < stands.size()) s.addStand(stands.get(j));
            stages.add(s);
            for (Attendee a : s.getAttendees()) {
                a.setOrdertime(ran.nextGaussian() * ServerConfig.totaltestseconds/4 + ServerConfig.totaltestseconds/2);
                //emulate attendee application setup
                a.setup(log);
            }

        }
        //put remaining stands around first stage
        while (j < stands.size()) {
            stages.get(0).addStand(stands.get(j));
            j++;
        }
        //emulate stand application setup
        for (Stand s : stands) {
            s.setup(log);
        }
        //wait for stands to finsh setup
        for (Stand s : stands) {
            boolean inProcess=true;
            while(inProcess){
                inProcess=!s.getReady();
            }
        }
        //have attendees choose their orders from menu by looking at global menu
        for (Stage s : stages) {
            s.chooseOrders();
        }
    }

    /**
     * This method will reset event to before orders are made
     */
    public void resetOrders(){
        for (Stage s : stages) {
            s.reset();
        }
        for (Stand s : stands) {
            s.reset();
        }
    }

    /**
     * This method will calculate performance of system by comparing to situation without it
     * @param systemOn if true, attendees can use the applications to order
     */
    public void setSystemOn(boolean systemOn){
        for (Stage s : stages) {
            s.setSystemOn(systemOn);
        }
    }

    /**
     * This method will start stages (pools of attendees) and stands
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        ArrayList<Thread> standThreads=new ArrayList<>();
        for (Stand s : stands) {
            Thread t = new Thread(s);
            standThreads.add(t);
            t.start();
        }
        ArrayList<Thread> stageThreads=new ArrayList<>();
        for (Stage s : stages) {
            Thread t = new Thread(s);
            stageThreads.add(t);
            t.start();
        }
        for (Thread t : stageThreads) {
            t.join();
        }
        for (Thread t : standThreads) {
            t.join();
        }
    }

    /**
     * This method will check if all orders made by attendees were received and processed by stands
     */
    public void checkOrderIds() {
        ArrayList<Integer> orders = new ArrayList<>();
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                orders.add(a.getOrderid());
            }
        }
        ArrayList<Integer> ordersDone = new ArrayList<>();
        for (Stand st : stands) {
            for (CommonOrder o : st.getReadyOrders()) {
                if (orders.contains(o.getId())) {
                    ordersDone.add(o.getId());
                }
            }
        }
        orders.removeAll(ordersDone);
        log.info("Unresolved orders: " + orders.size());
    }

    /**
     * In case of systemOn
     * @return the time walked to the stands
     */
    public double getTotalWalkingTime() {
        double walkingTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                walkingTime += a.getWalkingTime();
            }
        }
        log.info("Total time walked to stands: " + walkingTime);
        return walkingTime;
    }

    /**
     * In case of systemOff
     * @return time waited in front of stands
     */
    public double getTotalQueueTime(){
        double queueTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                queueTime += a.getQueueTime();
            }
        }
        log.info("Total time waited in front of stand: " + queueTime);
        return queueTime;
    }
    /**
     * In case of systemOff
     * @return time walked to stands before ordering
     */
    public double getTotalBetweenOrderTime(){
        double betweenOrderTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                betweenOrderTime += a.getBetweenOrderTime();
            }
        }
        log.info("Total time walked to stand: " + betweenOrderTime);
        return betweenOrderTime;
    }
    /**
     * In case of systemOn
     * @return time waited before going to stands (time spend as attendees wish)
     */
    public double getTotalWaitingTime() {
        double waitingTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                waitingTime += a.getWaitingTime();
            }
        }
        log.info("Total time waited before going to get order: " + waitingTime);
        return waitingTime;
    }

    /**
     * @return time between before getting food
     */
    public double getTotalOrderTime(){
        double orderTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                orderTime += a.getTotalTime();
            }
        }
        log.info("Total time between and getting order: " + orderTime);
        return orderTime;
    }

    /**
     * delete teststands
     */
    public void end() {
        for (Stand s : stands) {
            s.delete().subscribe(
                    o -> log.info("Stand " + s.getStandName() + " deleted"),
                    throwable -> log.error(throwable.getMessage())
            );
        }
    }

}
