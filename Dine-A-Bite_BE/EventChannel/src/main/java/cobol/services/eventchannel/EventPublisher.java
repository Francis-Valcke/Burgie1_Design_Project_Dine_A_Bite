package cobol.services.eventchannel;

public class EventPublisher {

    /**
     * @param e the event
     *
     * publishes an event
     */
    protected void Publish(Event e) {
        EventBroker.getInstance().addEvent(this, e);
    }
}
