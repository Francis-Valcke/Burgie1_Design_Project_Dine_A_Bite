package cobol.services.eventchannel;

import cobol.commons.Event;
import cobol.commons.ResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
     * @param name test value
     * @return hello world
     * <p>
     * This is a test function
     */
    @GetMapping("/test")
    public String test(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    /**
     * @param types channels the caller wants to subscribe to, types are separated by ','.
     * @return The unique id of the event subscriber stub
     * <p>
     * The callee sends this request along with the channels it wants to subscribe to. A stub is created and gets a
     * unique id. This is returned to the callee.
     */
    @GetMapping("/registerSubscriber")
    public int register(@RequestParam(value = "types", defaultValue = "") String types) {
        EventSubscriber newSubscriber = new EventSubscriber(types);
        newSubscriber.subscribe();
        return newSubscriber.getId();
    }

    /**
     * @param stubId The id to identify the subscriberstub
     * @param type   the channels the stub has to subscribe to
     *               <p>
     *               This method allows subscribers to subscribe to channels they were previously not subscribed to.
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
            for (String t : tempList) {
                typeList.add(t);
            }
            broker.unSubscribe(subscriber, typeList);
        }
    }


    /**
     * @param e The event to publish
     *          <p>
     *          Receives an event in JSON format, forwards it to the proper channels
     */
    @PostMapping(value = "/publishEvent", consumes = "application/json")
    public void publish(@RequestBody Event e) {
        EventPublisher.Publish(e);
    }

    /**
     * @param id unique id of stub
     * @return the events that were received by the stub since the last poll
     */
    @GetMapping("/events")
    public ResponseEntity events(@RequestParam(value = "id") int id) throws JsonProcessingException {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(id);
        List<Event> response = subscriber.getUnhandledEvents();
        ObjectMapper mapper = new ObjectMapper();
        String responseArray = mapper.writeValueAsString(response);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details(responseArray)
                        .build().generateResponse()
        );
    }
}