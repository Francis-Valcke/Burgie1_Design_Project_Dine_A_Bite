package cobol.services.authentication;

import cobol.commons.security.Role;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private UserRepository users;
    private PasswordEncoder passwordEncoder;

    /**
     * Debug methods for placing some initial users into the database.
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        users.saveAndFlush(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("adminadmin"))
                        .roles(Arrays.asList(Role.USER, Role.STAND, Role.ADMIN))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("useruser"))
                        .roles(Collections.singletonList(Role.USER))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("stand")
                        .password(passwordEncoder.encode("standstand"))
                        .roles(Arrays.asList(Role.USER, Role.STAND))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("OrderManager")
                        .password(passwordEncoder.encode("OrderManager"))
                        .roles(Collections.singletonList(Role.APPLICATION))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("StandManager")
                        .password(passwordEncoder.encode("StandManager"))
                        .roles(Collections.singletonList(Role.APPLICATION))
                        .build()
        );
    }

    @Autowired
    public void setUsers(UserRepository users) {
        this.users = users;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
