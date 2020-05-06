package cobol.services.eventchannel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableScheduling
@EnableWebSecurity
@ComponentScan({"cobol.services.eventchannel", "cobol.commons"})
@SpringBootApplication
public class EventService {

    public static void main(String[] args) {
        EventBroker broker = EventBroker.getInstance();
        Thread brokerThread = new Thread(broker);
        brokerThread.start();
        SpringApplication.run(EventService.class, args);
    }

}