package cobol.services.standmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StandManager {

    public static final String authToken= "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTdGFuZE1hbmFnZXIiLCJyb2xlcyI6WyJBUFBMSUNBVElPTiJdLCJpYXQiOjE1ODg1NDE2NTMsImV4cCI6MTc0NjIyMTY1M30.RtY_OYCR-2K3APztc-Up8UUWNQJ7_YteY09f4e7phfM";

    public static final boolean localTest = true;

    public static final String ACURL = localTest ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
    public static final  String OMURL = localTest ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String SMURL = localTest ? "http://localhost:8082" : "http://cobol.idlab.ugent.be:8092";
    public static final String ECURL = localTest ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";

    public static void main(String[] args) {
        SpringApplication.run(StandManager.class, args);
    }
}