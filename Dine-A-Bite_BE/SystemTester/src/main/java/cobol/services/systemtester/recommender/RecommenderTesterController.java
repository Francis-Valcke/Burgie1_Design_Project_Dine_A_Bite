package cobol.services.systemtester.recommender;

import cobol.services.systemtester.EventSimulation;
import cobol.services.systemtester.ServerConfig;
import cobol.services.systemtester.stage.Attendee;
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
    public void setup() throws IOException {
        es = new EventSimulation();
        es.setup(ServerConfig.attendeeCount);
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
        es = new EventSimulation();
        es.setup(ServerConfig.attendeeCount);
        es.start();
        es.checkOrderIds();
        es.getTotalWaitingTime();
        es.getTotalWalkingTime();
        es.getTotalOrderTime();
        es.end();
    }

    public void initiateShutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
    }
}
