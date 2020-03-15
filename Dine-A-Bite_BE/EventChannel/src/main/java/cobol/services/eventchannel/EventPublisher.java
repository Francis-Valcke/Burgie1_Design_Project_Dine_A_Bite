package cobol.services.eventchannel;

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
