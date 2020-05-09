package cobol.services.authentication;

import cobol.commons.config.GlobalConfigurationBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableScheduling
@EnableAspectJAutoProxy
@EnableConfigurationProperties
@ComponentScan({"cobol.services.authentication", "cobol.commons"})
@SpringBootApplication
public class AuthenticationService {

    public static void main(String[] args){
        ApplicationContext ctxt = SpringApplication.run(AuthenticationService.class,args);
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
