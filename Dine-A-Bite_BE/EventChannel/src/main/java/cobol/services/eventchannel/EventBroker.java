package cobol.services.eventchannel;

import cobol.commons.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class EventBroker implements Runnable {
    private final HashMap<String, HashSet<EventSubscriber>> subscriberMap = new HashMap<>();
    private final HashMap<Integer, EventSubscriber> subscriberId = new HashMap<>();
    private final LinkedList<Event> queue = new LinkedList<>();
    private volatile boolean stop = false;
    private volatile boolean run = true;
    Logger logger = LoggerFactory.getLogger(EventBroker.class);

    private static final EventBroker ourInstance = new EventBroker();

    public static EventBroker getInstance() {
        return ourInstance;
    }

    private EventBroker() {

    }

    /**
     * @param subscriber The entity that wants to subscribe to a channel
     * @param typeArray  An array of strings, defining the channels to subscribe to
     *
     * This function adds a subscriber to the event channels it wants to listen to.
     */
    public void subscribe(EventSubscriber subscriber, List<String> typeArray) {
        for (String type : typeArray) {
            synchronized (subscriberId) {
                if (!subscriberId.containsKey(subscriber.getId())) {
                    subscriberId.put(subscriber.getId(), subscriber);
                }
            }
            synchronized (subscriberMap) {
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
    }

    /**
     * This functions removes a subscriber from channels it does not want to listen to anymore.
     *
     * @param subscriber The entity that wants to unsubscribe to a channel
     * @param typeArray  An array of strings, defining the channels to unsubscribe to
     */
    public void unSubscribe(EventSubscriber subscriber, List<String> typeArray) {
        for (String type : typeArray) {
            subscriber.removeType(type);
            synchronized (subscriberMap) {
                HashSet<EventSubscriber> typeSet = subscriberMap.get(type);
                if (typeSet != null) {
                    typeSet.remove(subscriber);
                }
            }
        }
    }

    /**
     * Calls the process function.
     *
     * @param e the event
     */
    protected void addEvent(Event e) {
        if (!stop) {
            synchronized (queue) {
                queue.add(e);
                queue.notifyAll();
            }
        }
    }

    /**
     * Sends the event to every subscriber listening to the channels the event is sent to.
     *
     * @param e the event
     */
    private void process(Event e) {
        List<String> types = e.getTypes();
        for (String type : types) {
            HashSet<EventSubscriber> typeSet;
            synchronized (subscriberMap) {
                typeSet = subscriberMap.get(type);
            }
            if (typeSet != null) {
                for (EventSubscriber s : typeSet) {
                    s.handleEvent(e);  //loops are possible
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            Event e;
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        logger.warn("Thread was interrupted!", ex);
                        Thread.currentThread().interrupt();
                    }
                }
                e = queue.poll();
            }
            if (e != null) {
                process(e);
            }
            if (!run || Thread.interrupted()) {
                return;
            }
        }

    }

    public void stop() {
        while (!queue.isEmpty()) {
            stop = true;
        }
        synchronized (queue) {
            queue.notifyAll();
        }
        stop = false;
        this.setRun();
    }

    /**
     * @param id the unique id of the subscriber
     * @return the subscriber stub that is associated with an subscriber that is not on the server.
     */
    public EventSubscriber getSubscriberStub(int id) {
        synchronized (subscriberId) {
            return subscriberId.get(id);
        }
    }

    private void setRun() {
        this.run = false;
    }

}
