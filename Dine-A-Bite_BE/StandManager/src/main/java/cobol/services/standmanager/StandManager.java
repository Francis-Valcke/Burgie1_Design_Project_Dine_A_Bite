package cobol.services.standmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class StandManager {
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(StandManager.class,args);

    }

}