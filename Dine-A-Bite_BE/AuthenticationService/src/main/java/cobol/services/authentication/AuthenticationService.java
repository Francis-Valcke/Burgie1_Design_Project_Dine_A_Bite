package cobol.services.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The Authentication Service is responsible for authenticating users and handling user related requestes.
 */
@SpringBootApplication
public class AuthenticationService {

    public static void main(String[] args){
        SpringApplication.run(AuthenticationService.class,args);
    }

    /**
     * Exposing a password encoder bean for later use.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
