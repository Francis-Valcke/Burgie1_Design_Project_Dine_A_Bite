package cobol.services.authentication;

import cobol.commons.security.Role;
import cobol.commons.security.exception.DuplicateUserException;
import cobol.services.authentication.controller.AuthenticationController;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import com.stripe.exception.StripeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Log4j2
@Component
public class DataInitializer implements CommandLineRunner {

    private AuthenticationHandler authenticationHandler;

    /**
     * Debug methods for placing some initial users into the database.
     *
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        try {
            authenticationHandler.createUser(new AuthenticationRequest("admin", "adminadmin"), Role.USER, Role.STAND, Role.ADMIN);
            authenticationHandler.createUser(new AuthenticationRequest("user", "useruser"), Role.USER);
            authenticationHandler.createUser(new AuthenticationRequest("stand", "standstand"), Role.USER, Role.STAND);
            authenticationHandler.createUser(new AuthenticationRequest("OrderManager", "OrderManager"), Role.APPLICATION);
            authenticationHandler.createUser(new AuthenticationRequest("StandManager", "StandManager"), Role.APPLICATION);
        } catch (Exception e) {
            //Silent fail
        }

    }

    @Autowired
    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }
}
