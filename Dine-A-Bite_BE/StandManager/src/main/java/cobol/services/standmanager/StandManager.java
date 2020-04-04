package cobol.services.standmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StandManager {

    public static final String authToken= "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI";

    public static final boolean localTest = false;

    public static final String ACURL = localTest ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
    public static final String SMURL = localTest ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final  String OMURL = localTest ? "http://localhost:8082" : "http://cobol.idlab.ugent.be:8092";
    public static final String ECURL = localTest ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";

    public static void main(String[] args) {
        SpringApplication.run(StandManager.class, args);
    }
}