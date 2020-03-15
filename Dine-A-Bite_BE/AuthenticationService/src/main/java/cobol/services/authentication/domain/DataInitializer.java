package cobol.services.authentication.domain;

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

    @Override
    public void run(String... args) throws Exception {
        users.saveAndFlush(
                User.builder()
                        .username("francis")
                        .password(passwordEncoder.encode("valcke"))
                        .role(Arrays.asList("USER", "ADMIN"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("thomas")
                        .password(passwordEncoder.encode("valcke"))
                        .role(Arrays.asList("USER"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("OrderManager")
                        .password(passwordEncoder.encode("OrderManager"))
                        .role(Arrays.asList("APPLICATION"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("StandManager")
                        .password(passwordEncoder.encode("StandManager"))
                        .role(Arrays.asList("APPLICATION"))
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
