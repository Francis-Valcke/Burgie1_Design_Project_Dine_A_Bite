package cobol.services.authentication;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.security.Role;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Log4j2
@Component
public class DataInitializer implements CommandLineRunner {

    private UserHandler userHandler;

    /**
     * Debug methods for placing some initial users into the database.
     *
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        try {
            userHandler.createUser(new AuthenticationRequest("admin", "adminadmin"), Role.USER, Role.STAND, Role.ADMIN);
            userHandler.createUser(new AuthenticationRequest("user", "useruser"), Role.USER);
            userHandler.createUser(new AuthenticationRequest("stand", "standstand"), Role.USER, Role.STAND);
            userHandler.createUser(new AuthenticationRequest("AuthenticationService", "AuthenticationService"), Role.APPLICATION);
            userHandler.createUser(new AuthenticationRequest("OrderManager", "OrderManager"), Role.APPLICATION);
            userHandler.createUser(new AuthenticationRequest("StandManager", "StandManager"), Role.APPLICATION);
            userHandler.createUser(new AuthenticationRequest("EventChannel", "EventChannel"), Role.APPLICATION);
            userHandler.createUser(new AuthenticationRequest("SystemTester", "SystemTester"), Role.APPLICATION);

        } catch (Exception e) {
            //Silent fail
        }

    }

    @Autowired
    public void setUserHandler(UserHandler userHandler) {
        this.userHandler = userHandler;
    }
}
