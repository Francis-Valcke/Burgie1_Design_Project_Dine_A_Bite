package cobol.services.eventchannel;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    @GetMapping("/registerSubcriber")
    public int register(@RequestParam(value="types", defaultValue = "") String types) {
        EventSubscriber newSubscriber = new EventSubscriber(types);
        newSubscriber.subscribe();
        return newSubscriber.getId();
    }

    /**
     *
     * @param stub_id The id to identify the subscriberstub
     * @param type the channels the stub has to subscribe to
     *
     * This method allows subscribers to subscribe to channels they were previously not subscribed to.
     */
    @GetMapping("/registerSubscriber/toChannel")
    public void toChannel(@RequestParam(value="id") int stub_id, @RequestParam(value="type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stub_id);
        subscriber.addType(type);
        broker.subscribe(subscriber, subscriber.getTypes());
    }

    /**
     *
     * @param stub_id id of the stub to desubscribe
     * @param type channels to desubscribe from, separated by commas. If none are given, stub desubscribes from all channels
     */
    @GetMapping("/deregisterSubscriber")
    public void deRegister(@RequestParam(value="id") int stub_id, @RequestParam(value="type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stub_id);
        if (type == "") {
            broker.unSubscribe(subscriber, subscriber.getTypes());
        }
        else {
            List<String> typeList = new ArrayList<>();
            String[] tempList = type.split(",");
            for (String t : tempList) {
                typeList.add(t);
            }
            broker.unSubscribe(subscriber, typeList);
        }
    }

    /**
     *
     * @param e event to publish
     *
     * publishes the event to the event broker. This must happen here, because the eventbroker is running on this
     * microservice. Keep in mind the application that publishes needs to import EventPublisher and Event.
     *
     */
    //@GetMapping("/publish")
    //public void publish(@RequestBody Event e) {
    //    EventPublisher.Publish(e);
    //}

    @PostMapping(value = "/publishEvent", consumes = "application/json")
    public void publish(@RequestBody Event e) {
        EventPublisher.Publish(e);
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