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
    }
    @GetMapping("/test")
    public void testStands(){
        es.setup(100);
    }
    @GetMapping("/end")
    public void end() {
        es.end();
    }

    @GetMapping("/create")
    public ResponseEntity create(@RequestParam int count) {

        for (int i = 0; i < count; i++) {
            Attendee attendee = new Attendee(0,0);
            attendees.add(attendee);
            attendee.create().subscribe(
                o -> log.info("User " + attendee.getId() + " created!"),
                throwable -> log.error(throwable.getMessage())
            );
        }

        return null;
    }

    @GetMapping("/authenticate")
    public ResponseEntity authenticate() {

        for (Attendee attendee : attendees) {
            attendee.authenticate().subscribe(
                o -> log.info("User " + attendee.getId() + " authenticated with token: " + o.getJSONObject("details").getString("token")),
                throwable -> log.error(throwable.getMessage())
            );
        }

        return null;
    }

    @GetMapping("/order")
    public ResponseEntity order() {

        for (Attendee attendee : attendees) {
            attendee.getGlobalMenu().subscribe(
                    items -> attendee.placeRandomOrder(items, 1).subscribe(
                            log::info,
                            log::error
                    )
            );
        }

        return null;
    }

    @PostConstruct
    public void run() throws UnirestException {

    }
}
