package cobol.services.eventchannel;

public class EventPublisher {
    void Publish(Event e) {
        EventBroker.getInstance().addEvent(this, e);
    }
}
