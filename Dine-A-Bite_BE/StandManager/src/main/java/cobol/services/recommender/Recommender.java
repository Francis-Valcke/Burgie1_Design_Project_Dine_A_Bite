package cobol.services.recommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Recommender {
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Recommender.class,args);

    }

}