package cobol.services.eventchannel;

import cobol.commons.BetterResponseModel;
import cobol.commons.Event;
import cobol.commons.ResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cobol.commons.ResponseModel.status.OK;

@RestController
public class EventController {

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "EventChannel is alive!"
     */
    @GetMapping("/pingEC")
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("EventChannel is alive!")
                        .build().generateResponse()
        );
    }

    /**
     * This is a test function
     *
     * @param name test value
     * @return hello world
     */
    @GetMapping("/test")
    public String test(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    /**
     * The callee sends this request along with the channels it wants to subscribe to. A stub is created and gets a
     * unique id. This is returned to the callee.
     *
     * @param types channels the caller wants to subscribe to, types are separated by ','.
     * @return The unique id of the event subscriber stub
     */
    @GetMapping("/registerSubscriber")
    public int register(@RequestParam(value = "types", defaultValue = "") String types) {
        EventSubscriber newSubscriber = new EventSubscriber(types);
        newSubscriber.subscribe();
        return newSubscriber.getId();
    }

    /**
     * This method allows subscribers to subscribe to channels they were previously not subscribed to.
     *
     * @param stubId The id to identify the subscriberstub
     * @param type   the channels the stub has to subscribe to
     */
    @GetMapping("/registerSubscriber/toChannel")
    public void toChannel(@RequestParam(value = "id") int stubId, @RequestParam(value = "type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stubId);
        subscriber.addType(type);
        List<String> newTypes = new ArrayList<>();
        newTypes.add(type);
        broker.subscribe(subscriber, newTypes);
    }

    /**
     * @param stubId id of the stub to desubscribe
     * @param type   channels to desubscribe from, separated by commas. If none are given, stub desubscribes from all channels
     */
    @GetMapping("/deregisterSubscriber")
    public void deRegister(@RequestParam(value = "id") int stubId, @RequestParam(value = "type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stubId);
        if (type.equals("")) {
            broker.unSubscribe(subscriber, subscriber.getTypes());
        } else {
            List<String> typeList = new ArrayList<>();
            String[] tempList = type.split(",");
            Collections.addAll(typeList, tempList);
            broker.unSubscribe(subscriber, typeList);
        }
    }


    /**
     * Receives an event in JSON format, forwards it to the proper channels
     *
     * @param e The event to publish
     */
    @PostMapping(value = "/publishEvent", consumes = "application/json")
    public void publish(@RequestBody Event e) {
        EventPublisher.publish(e);
    }

    /**
     * @param id unique id of stub
     * @return the events that were received by the stub since the last poll
     */
    @GetMapping("/events")
    public BetterResponseModel<List<Event>> events(@RequestParam(value = "id") int id) {
        EventBroker broker = EventBroker.getInstance();
        try{
            EventSubscriber subscriber = broker.getSubscriberStub(id);
            List<Event> response = subscriber.getUnhandledEvents();
            return BetterResponseModel.ok("Successfully retrieved events", response);
        }
        catch(Exception e){
            e.printStackTrace();
            return BetterResponseModel.error("No such subscriber id, request a new id", e);
        }
    }
}