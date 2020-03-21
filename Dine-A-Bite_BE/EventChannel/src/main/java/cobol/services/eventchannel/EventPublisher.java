package cobol.services.eventchannel;

import cobol.commons.Event;

public class EventPublisher {

    /**
     * @param e the event
     *
     * publishes an event
     */
    static void Publish(Event e) {
        EventBroker.getInstance().addEvent(e);
    }
}
