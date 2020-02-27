package cobol.services.eventchannel;

public interface EventSubscriber {
    void subscribe(EventSubscriber this, String[] typeArray);
    void handleEvent(Event e);
}
