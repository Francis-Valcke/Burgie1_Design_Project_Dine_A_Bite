package cobol.services.eventchannel;

import cobol.commons.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stub class that represents EventSubscribers from different modules
 */
public class EventSubscriber {
    private static int numSubscribers = 0;
    private int id;
    private List<String> types = new ArrayList<>();
    private List<Event> unhandledEvents = new ArrayList<>();

    EventSubscriber(String types) {
        id = numSubscribers;
        numSubscribers++;
        String[] tempTypes = types.split(",");
        Collections.addAll(this.types, tempTypes);
    }

    /**
     * lets the stub subscribe to the appropriate channels
     */
    void subscribe() {
        EventBroker broker = EventBroker.getInstance();
        broker.subscribe(this, this.types);
    }

    /**
     * add the event to the unhandled event list
     *
     * @param e the event
     */
    void handleEvent(Event e) {
        unhandledEvents.add(e);
    }
    //-- Getters and Setters --//

    public void addType(String type) {
        this.types.add(type);
    }

    public void removeType(String type) {
        this.types.remove(type);
    }

    public List<String> getTypes() {
        return this.types;
    }

    public int getId() {
        return id;
    }

    public List<Event> getUnhandledEvents() {
        List<Event> ret = unhandledEvents;
        unhandledEvents = new ArrayList<>();
        return ret;
    }
}
