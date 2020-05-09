package cobol.services.standmanager.config;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.stub.AuthenticationServiceStub;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "standmanager")
public class ConfigurationBean {

    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    private String username;
    private String password;

    /**
     * When the authentication service becomes available, try to authenticate.
     */
    @PostConstruct
    public void run(){
        authenticationServiceStub.doOnAvailable(() -> {
            // TODO update token in the stub
            authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
        });
    }
}
