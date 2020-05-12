package cobol.services.standmanager.config;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.stub.Action;
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


//    /**
//     * When the authentication service becomes available, try to authenticate.
//     */
//    @PostConstruct
//    public void run(){
//        authenticationServiceStub.doOnAvailable(() -> {
//
//            authenticationHandler.authenticateAdvice();
//
//            if (authenticationHandler.isAuthenticated()){
//                return true;
//            } else {
//                return false;
//            }
//
//        }, Action.PRIORITY_NORMAL, true, true);
//    }
}
