package cobol.services.authentication.config;


import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.config.GlobalConfigurationBean;
import cobol.commons.stub.AuthenticationServiceStub;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "authenticationservice")
public class ConfigurationBean {

    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    private String username;
    private String password;
    private String stripeSecretApiKey;
    private String stripePublicApiKey;

    ///**
    // * When the authentication service becomes available, try to authenticate.
    // */
    //@PostConstruct
    //public void run(){
    //    authenticationServiceStub.doOnAvailable(() -> {
    //
    //        try {
    //
    //            BetterResponseModel<String> response = authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
    //            if (response.isOk()) {
    //
    //                authenticationServiceStub.setAuthorizationToken(Objects.requireNonNull(response).getPayload());
    //                log.info("Successfully authenticated this module.");
    //
    //            } else throw response.getException();
    //
    //        } catch (Throwable throwable) {
    //            log.error("Could not authenticate.", throwable);
    //        }
    //
    //    });
    //}
}