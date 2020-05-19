package cobol.services.eventchannel;

import cobol.commons.Event;

/**
 * Stub class for publishing events to the broker
 */
public class EventPublisher {

    private EventPublisher() {}

    /**
     * publishes an event
     *
     * @param e the event
     */
    static void publish(Event e) {
        EventBroker.getInstance().addEvent(e);
    }
}
