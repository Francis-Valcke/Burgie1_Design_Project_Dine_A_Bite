package cobol.services.eventchannel;

import cobol.commons.Event;

public class EventPublisher {

    /**
     * publishes an event
     *
     * @param e the event
     */
    static void Publish(Event e) {
        EventBroker.getInstance().addEvent(e);
    }
}
