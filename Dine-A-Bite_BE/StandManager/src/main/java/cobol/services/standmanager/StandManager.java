package cobol.services.standmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StandManager {
    public static boolean test = true;

    public static final String authToken= "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI";

    public static String ACURL;
    public static String SMURL;
    public static String OMURL;
    public static String ECURL;

    public static void main(String[] args) {
        ACURL = test ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
        OMURL = test ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
        SMURL = test ? "http://localhost:8082" : "http://cobol.idlab.ugent.be:8092";
        ECURL = test ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";
        SpringApplication.run(StandManager.class, args);
    }
}