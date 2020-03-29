package cobol.services.eventchannel;

import cobol.commons.Event;

import java.util.ArrayList;
import java.util.List;


public class EventSubscriber {
    private static int numSubscribers = 0;
    private int id;
    private List<String> types = new ArrayList<>();
    private List<Event> unhandledEvents = new ArrayList<>();

    EventSubscriber(String types) {
        id = numSubscribers;
        numSubscribers++;
        String[] temp_types = types.split(",");
        for (String type : temp_types) {
            this.types.add(type);
        }
    }

    /**
     * lets the stub subscribe to the appropriate channels
     */
    void subscribe() {
        EventBroker broker = EventBroker.getInstance();
        broker.subscribe(this, this.types);
    }

    /**
     * @param e the event
     *          <p>
     *          add the event to the unhandled event list
     */
    void handleEvent(Event e) {
        unhandledEvents.add(e);
        System.out.println("Received event" + e.getEventData());
    }

    public void addType(String type) {
        this.types.add(type);
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
