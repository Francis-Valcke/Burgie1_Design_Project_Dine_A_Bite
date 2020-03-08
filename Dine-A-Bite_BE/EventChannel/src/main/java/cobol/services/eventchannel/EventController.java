package cobol.services.eventchannel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EventController {

    /**
     *
     * @param name test value
     * @return hello world
     *
     * This is a test function
     */
    @GetMapping("/test")
    public String test(@RequestParam(value="name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    /**
     *
     * @param types channels the caller wants to subscribe to, types are separated by ','.
     * @return The unique id of the event subscriber stub
     *
     * The callee sends this request along with the channels it wants to subscribe to. A stub is created and gets a
     * unique id. This is returned to the callee.
     *
     */
    @GetMapping("/register")
    public int register(@RequestParam(value="types", defaultValue = "") String types) {
        EventSubscriber newSubscriber = new EventSubscriber(types);
        newSubscriber.subscribe();
        return newSubscriber.getId();
    }

    /**
     *
     * @param id unique id of stub
     * @return the events that were received by the stub since the last poll
     */
    @GetMapping("/events")
    public List<Event> events(@RequestParam(value="id") int id) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(id);
        return subscriber.getUnhandledEvents();
    }
}