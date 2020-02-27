package cobol.services.eventchannel;

import java.util.HashMap;
import java.util.HashSet;

public class EventBroker {
    private HashMap<String, HashSet<EventSubscriber>> subscriberMap = new HashMap<>();

    private final static EventBroker ourInstance = new EventBroker();

    public static EventBroker getInstance() {
        return ourInstance;
    }

    private EventBroker() {

    }

    public void subscribe(EventSubscriber subscriber, String[] typeArray) {
        for (String type : typeArray) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
            if (typeSet == null) {
                typeSet = new HashSet<>();
                typeSet.add(subscriber);
                subscriberMap.put(type, typeSet);
            } else {
                typeSet.add(subscriber);
            }
        }
    }

    public void unSubscribe(EventSubscriber subscriber, String[] typeArray) {
        for (String type : typeArray) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
            if (typeSet == null) {
                continue; //silently discard wrong type, should we define an exception for this?
            } else {
                typeSet.remove(subscriber);
            }
        }
    }

    protected void addEvent(EventPublisher source, Event e) {
        process(source, e);
    }

    private void process(EventPublisher source, Event e) {
        String[] types = e.getTypes();
        for (String type : types) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
                if (typeSet != null) {
                    for (EventSubscriber s : typeSet) {
                        if (s != source) {
                            s.handleEvent(e);
                        }
                }
            }
        }
    }

}
