package cobol.services.ordermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class OrderManager {

    public static final boolean test = false;

    public static final String ACURL = test ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
    public static final String SMURL =  test ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String OMURL = test ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String ECURL = test ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";

    public static void main(String[] args) {
        SpringApplication.run(OrderManager.class, args);
    }

}