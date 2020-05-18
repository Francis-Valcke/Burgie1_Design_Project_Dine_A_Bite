package cobol.services.systemtester;

import cobol.commons.order.CommonOrder;
import cobol.services.systemtester.stage.Attendee;
import cobol.services.systemtester.stage.Stage;
import cobol.services.systemtester.stage.Stand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
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
    private final int stageCount = 5;

    public EventSimulation() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("dataset.json");
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        stands = (ArrayList<Stand>) mapper.readValue(body, new TypeReference<List<Stand>>() {
        });

    }

    public void setup(int size) {
        Random ran = new Random();
        int j = 0;
        //create stages and spread stands around stages
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
                a.setup(log);
            }

        }
        //put remaining stands around first stage
        while (j < stands.size()) {
            stages.get(0).addStand(stands.get(j));
            j++;
        }
        for (Stand s : stands) {
            s.setup(log);
        }
    }

    public void start() throws InterruptedException {
        for (Stand s : stands) {
            s.start();
        }
        for (Stage s : stages) {
            s.start();
        }
        for (Stage s : stages) {
            s.join();
        }
        for (Stand s : stands) {
            s.join();
        }
    }

    /**
     * check if all orders made by attendees were received by stands
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
            for (CommonOrder o : st.getOrders()) {
                if (orders.contains(o.getId())) {
                    ordersDone.add(o.getId());
                }
            }
        }
        orders.removeAll(ordersDone);
        log.info("Unresolved orders: " + orders.size());
    }

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

    public double getTotalWaitingTime() {
        double waitingTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                waitingTime += a.getWaitingTime();
            }
        }
        log.info("Total time waited before getting order: " + waitingTime);
        return waitingTime;
    }
    public double getTotalOrderTime(){
        double orderTime = 0;
        for (Stage s : stages) {
            for (Attendee a : s.getAttendees()) {
                orderTime += a.getOrderReadyTime();
            }
        }
        log.info("Total time between and getting order: " + orderTime);
        return orderTime;
    }

    public void end() {
        for (Stand s : stands) {
            s.delete().subscribe(
                    o -> log.info("Stand " + s.getStandName() + " deleted"),
                    throwable -> log.error(throwable.getMessage())
            );
        }
    }

}
