package cobol.services.standmanager.standhandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;
import java.util.ArrayList;

@SpringBootApplication
public class StandManager {

    public static void main(String[] args) throws InterruptedException, SQLException {
        SpringApplication.run(StandManager.class,args);

    }

}