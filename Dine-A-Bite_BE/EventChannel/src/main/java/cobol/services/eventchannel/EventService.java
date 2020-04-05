package cobol.services.eventchannel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class EventService {

    public static void main(String[] args) {
        EventBroker broker = EventBroker.getInstance();
        Thread brokerThread = new Thread(broker);
        brokerThread.start();
        SpringApplication.run(EventService.class, args);
    }

}