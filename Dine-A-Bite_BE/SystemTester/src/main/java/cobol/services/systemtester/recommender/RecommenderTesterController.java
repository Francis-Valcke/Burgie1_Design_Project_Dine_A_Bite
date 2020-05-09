package cobol.services.systemtester.recommender;

import cobol.services.systemtester.EventSimulation;
import cobol.services.systemtester.stage.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.reactivex.Single;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.TimeUnit;

@RestController
@Log4j2
public class RecommenderTesterController {
    @Autowired
    private ApplicationContext appContext;


    EventSimulation es;

    List<Attendee> attendees = new ArrayList<>();
    @GetMapping("/setup")
    public void setup() throws IOException {
        es=new EventSimulation();
        es.setup(10);
    }
    @GetMapping("/test")
    public void testStands() throws InterruptedException {
        es.start();
    }
    @GetMapping("/end")
    public void end() {
        es.end();
    }


    @PostConstruct
    public void run() throws IOException, InterruptedException {
        es=new EventSimulation();
        es.setup(100);
        es.start();
        es.checkOrderIds();
        es.getTotalWaitingTime();
        es.getTotalWalkingTime();
        es.end();
    }
    public void initiateShutdown(int returnCode){
        SpringApplication.exit(appContext, () -> returnCode);
    }
}
