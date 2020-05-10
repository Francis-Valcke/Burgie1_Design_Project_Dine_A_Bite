package cobol.services.ordermanager.config;


import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.stub.Action;
import cobol.commons.stub.AuthenticationServiceStub;
import cobol.commons.stub.StandManagerStub;
import cobol.services.ordermanager.MenuHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "ordermanager")
public class ConfigurationBean {

    @Autowired
    MenuHandler menuHandler;
    @Autowired
    StandManagerStub standManagerStub;
    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    private String username;
    private String password;
    boolean unitTest;

    /**
     * When the authentication service becomes available, try to authenticate.
     */
    @PostConstruct
    public void run(){
        authenticationServiceStub.doOnAvailable(() -> {

            try {

                BetterResponseModel<String> response = authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
                if (response.isOk()) {

                    authenticationServiceStub.setAuthorizationToken(Objects.requireNonNull(response).getPayload());
                    log.info("Successfully authenticated this module.");

                } else throw response.getException();

            } catch (Throwable throwable) {
                log.error("Could not authenticate.", throwable);
            }

        }, Action.PRIORITY_HIGHEST, true);
    }

    @PostConstruct
    public void postConstruct(){
        standManagerStub.doOnAvailable(() -> {
            try {
                menuHandler.updateStandManager();
                log.info("Stand manager has been updated successfully");
            } catch (JsonProcessingException e) {
                log.error("Could not update stand manager after becoming available again.", e);
            }
        }, Action.PRIORITY_HIGH, true);
    }

}