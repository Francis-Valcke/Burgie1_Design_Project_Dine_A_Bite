package cobol.services.systemtester;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class SystemTester {

    public static void main(String[] args) throws InterruptedException, SQLException {
        SpringApplication.run(SystemTester.class,args);

    }

}