package cobol.services.ordermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class OrderManager {

    public static final String authToken= "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJBUFBMSUNBVElPTiJdLCJpYXQiOjE1ODg0MzYyMzcsImV4cCI6MTc0NjExNjIzN30.kW4au3AIHA918DEJ2QgE8uXS1SNX9rCja1Bx3Zltgzw";

    public static final boolean localTest = true;

    public static final String ACURL = localTest ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
    public static final String OMURL = localTest ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String SMURL = localTest ? "http://localhost:8082" : "http://cobol.idlab.ugent.be:8092";
    public static final String ECURL = localTest ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";

    public static void main(String[] args) {

        SpringApplication.run(OrderManager.class, args);

    }

}