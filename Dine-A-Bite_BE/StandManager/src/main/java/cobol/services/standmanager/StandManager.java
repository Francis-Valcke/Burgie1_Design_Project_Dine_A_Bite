package cobol.services.standmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.annotation.PostConstruct;

@EnableWebSecurity
@EnableScheduling
@EnableConfigurationProperties
@ComponentScan({"cobol.services.standmanager", "cobol.commons"})
@SpringBootApplication
public class StandManager {



    public static final String authToken= "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTdGFuZE1hbmFnZXIiLCJyb2xlcyI6WyJBUFBMSUNBVElPTiJdLCJpYXQiOjE1ODg1NDE2NTMsImV4cCI6MTc0NjIyMTY1M30.RtY_OYCR-2K3APztc-Up8UUWNQJ7_YteY09f4e7phfM";

    public static void main(String[] args) {
        SpringApplication.run(StandManager.class, args);
    }



}