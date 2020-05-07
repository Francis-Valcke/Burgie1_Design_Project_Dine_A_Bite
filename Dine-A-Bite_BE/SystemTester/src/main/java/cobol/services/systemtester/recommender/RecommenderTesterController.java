package cobol.services.systemtester.recommender;

import cobol.services.systemtester.EventSimulation;
import cobol.services.systemtester.stage.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.reactivex.Single;
import lombok.extern.log4j.Log4j2;
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
    EventSimulation es;

    List<Attendee> attendees = new ArrayList<>();
    @GetMapping("/setup")
    public void setup() throws IOException {
        es=new EventSimulation();
        es.setup(100);
    }
    @GetMapping("/test")
    public void testStands(){


        es.start();
    }
    @GetMapping("/end")
    public void end() {
        es.end();
    }


    @PostConstruct
    public void run() throws UnirestException {

    }
}
