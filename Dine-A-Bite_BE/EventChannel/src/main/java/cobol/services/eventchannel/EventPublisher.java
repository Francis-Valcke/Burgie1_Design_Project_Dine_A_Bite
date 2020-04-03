package cobol.services.eventchannel;

import cobol.commons.Event;

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
