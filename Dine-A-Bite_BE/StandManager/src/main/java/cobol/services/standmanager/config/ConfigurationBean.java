package cobol.services.standmanager.config;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.stub.AuthenticationHandler;
import cobol.commons.stub.AuthenticationServiceStub;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "standmanager")
public class ConfigurationBean {

    @Autowired
    AuthenticationHandler authenticationHandler;
    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    private String username;
    private String password;

    /**
     * These credentials will be used to try to authenticate
     */
    @PostConstruct
    public void setupAuthentication(){
        authenticationHandler.setUsername(username);
        authenticationHandler.setPassword(password);
    }

    /**
     * When the authentication service becomes available, try to authenticate.
     */
//    @PostConstruct
//    public void run(){
//        authenticationServiceStub.doOnAvailable(() -> {
//
//            try {
//
//                BetterResponseModel<String> response = authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
//                if (response.isOk()) {
//
//                    authenticationServiceStub.setAuthorizationToken(Objects.requireNonNull(response).getPayload());
//                    log.info("Successfully authenticated this module.");
//
//                } else throw response.getException();
//
//            } catch (Throwable throwable) {
//                log.error("Could not authenticate.", throwable);
//            }
//
//        });
//    }
}
