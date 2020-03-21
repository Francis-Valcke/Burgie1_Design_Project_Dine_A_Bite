package cobol.services.eventchannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EventBroker {
    private HashMap<String, HashSet<EventSubscriber>> subscriberMap = new HashMap<>();
    private HashMap<Integer, EventSubscriber> subscriberId = new HashMap<>();

    private final static EventBroker ourInstance = new EventBroker();

    public static EventBroker getInstance() {
        return ourInstance;
    }

    private EventBroker() {

    }

    /**
     *
     * @param subscriber The entity that wants to subscribe to a channel
     * @param typeArray An array of strings, defining the channels to subscribe to
     *
     *
     * This function adds a subscriber to the event channels it wants to listen to.
     */
    public void subscribe(EventSubscriber subscriber, List<String> typeArray) {
        for (String type : typeArray) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
            if (!subscriberId.containsKey(subscriber.getId())) {
                subscriberId.put(subscriber.getId(), subscriber);
            }
            if (typeSet == null) {
                typeSet = new HashSet<>();
                typeSet.add(subscriber);
                subscriberMap.put(type, typeSet);
            } else {
                if (!typeSet.contains(subscriber)) {
                    typeSet.add(subscriber);
                }
            }
        }
    }

    /**
     *
     * @param subscriber The entity that wants to unsubscribe to a channel
     * @param typeArray An array of strings, defining the channels to unsubscribe to
     *
     * This functions removes a subscriber from channels it does not want to listen to anymore.
     */
    public void unSubscribe(EventSubscriber subscriber, List<String> typeArray) {
        for (String type : typeArray) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
            if (typeSet == null) {
                continue; //silently discard wrong type, should we define an exception for this?
            } else {
                typeSet.remove(subscriber);
            }
        }
    }

    /**
     *
     * @param e the event
     *
     * Calls the process function.
     */
    protected void addEvent(Event e) {
        System.out.println("Event was received with data: " + e.orderData); //TODO: remove, this is for testing
        process(e);
    }

    /**
     *
     * @param e the event
     *
     * Sends the event to every subscriber listening to the channels the event is sent to.
     */
    private void process(Event e) {
        String[] types = e.getTypes();
        System.out.println("Event recieved with data: " + e.getOrderData()); //visual feedback
        for (String type : types) {
            HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
                if (typeSet != null) {
                    for (EventSubscriber s : typeSet) {
                        s.handleEvent(e);  //loops are possible
                }
            }
        }
    }

    /**
     *
     * @param id the unique id of the subscriber
     * @return the subscriber stub that is associated with an subscriber that is not on the server.
     *
     */
    public EventSubscriber getSubscriberStub(int id) {
        return subscriberId.get(id);
    }

}
