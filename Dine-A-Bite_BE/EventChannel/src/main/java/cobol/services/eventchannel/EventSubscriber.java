package cobol.services.eventchannel;

import java.util.ArrayList;
import java.util.List;


public class EventSubscriber {
    private static int numSubscribers = 0;
    private int id;
    private String[] types;
    private List<Event> unhandledEvents = new ArrayList<>();

    EventSubscriber(String types) {
        id = numSubscribers;
        numSubscribers++;
        this.types = types.split(",");
    }

    void subscribe(EventSubscriber this) {
        EventBroker broker = EventBroker.getInstance();
        broker.subscribe(this, this.types);
    }
    void handleEvent(Event e) {
        unhandledEvents.add(e);
    }

    public int getId() {
        return id;
    }

    public List<Event> getUnhandledEvents() {
        return unhandledEvents;
    }
}
