package cobol.services.authentication;

import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
                        .username("francis")
                        .password(passwordEncoder.encode("valcke"))
                        .role(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("thomas")
                        .password(passwordEncoder.encode("valcke"))
                        .role(Arrays.asList("ROLE_USER"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("OrderManager")
                        .password(passwordEncoder.encode("OrderManager"))
                        .role(Arrays.asList("ROLE_APPLICATION"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("StandManager")
                        .password(passwordEncoder.encode("StandManager"))
                        .role(Arrays.asList("ROLE_APPLICATION"))
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
