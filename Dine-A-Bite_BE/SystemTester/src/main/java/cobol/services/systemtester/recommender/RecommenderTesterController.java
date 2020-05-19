package cobol.services.systemtester.recommender;

import cobol.services.systemtester.EventSimulation;
import cobol.services.systemtester.ModuleHandler;
import cobol.services.systemtester.ServerConfig;
import cobol.services.systemtester.stage.Attendee;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Log4j2
public class RecommenderTesterController {
    EventSimulation es;
    @Autowired
    private ApplicationContext appContext;

    @GetMapping("/setup")
    public void setup() throws IOException, UnirestException {
        es = new EventSimulation();
        es.setup(ServerConfig.attendeeCount);
    }

    @GetMapping("/test")
    public void testStands() throws InterruptedException{
        es.start();
    }

    @GetMapping("/end")
    public void end() {
        es.end();
    }


    @PostConstruct
    public void run() throws IOException, InterruptedException {
        es = new EventSimulation();
        es.setup(ServerConfig.attendeeCount);
        es.setSystemOn(true);
        es.start();
        es.checkOrderIds();
        double waitingTime= es.getTotalWaitingTime();
        double walkingTime=es.getTotalWalkingTime();
        double totalTime=es.getTotalOrderTime();
        es.resetOrders();
        es.setSystemOn(false);
        es.start();
        es.checkOrderIds();
        System.out.println("without system times:");
        es.getTotalBetweenOrderTime();
        es.getTotalQueueTime();
        es.getTotalOrderTime();
        System.out.println("with system times:");
        System.out.println("waiting time: "+waitingTime);
        System.out.println("walking time: "+walkingTime);
        System.out.println("total time: "+totalTime);
        es.end();
    }

    public void initiateShutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
    }
}
