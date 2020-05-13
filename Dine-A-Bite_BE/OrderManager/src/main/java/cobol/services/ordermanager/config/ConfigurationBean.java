package cobol.services.ordermanager.config;


import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.stub.*;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.OrderProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.naming.CommunicationException;
import java.util.Objects;

@Aspect
@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "ordermanager")
public class ConfigurationBean {

    @Autowired
    StandManagerStub standManagerStub;
    @Autowired
    EventChannelStub eventChannelStub;
    @Autowired
    AuthenticationServiceStub authenticationServiceStub;
    @Autowired
    AuthenticationHandler authenticationHandler;

    @Autowired
    MenuHandler menuHandler;
    @Autowired
    OrderProcessor orderProcessor;

    private String username;
    private String password;

    boolean unitTest;

    private boolean subscribed = false;

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


    /**
     * When the event channel becomes available, try to retrieve a subscriberID
     */
    @PostConstruct
    private void getSubscriberId() {

        eventChannelStub.doOnAvailable(() -> {

            boolean success;

            try {
                log.info("Trying to register at event channel");

                int id = eventChannelStub.getRegisterSubscriber("");
                orderProcessor.setSubscriberId(id);
                subscribed = true;
                success = true;
                log.info("Successfully registered with subscriber ID from event channel: ID = " + id);
            } catch (Exception e) {
                subscribed = false;
                success = false;
                log.info("Could not request subscriber ID from event channel.");
                log.debug("Could not request subscriber ID from event channel.", e);
            }

            return success;

        }, Action.PRIORITY_NORMAL, false, true);


        eventChannelStub.doOnUnavailable(()->{
            subscribed = false;
            return true;
        }, Action.PRIORITY_NORMAL, false, false);
    }

    /**
     * When the stand manager becomes available, try to update its schedulers
     */
    @PostConstruct
    public void updateStandManager(){

        standManagerStub.doOnAvailable(() -> {

            boolean success;

            try {
                menuHandler.updateStandManager();
                success = true;
                log.info("Stand manager has been updated successfully");
            } catch (JsonProcessingException e) {
                success = false;
                log.info("Could not update stand manager after becoming available again.");
                log.error("Could not update stand manager after becoming available again.", e);
            }

            return success;

        }, Action.PRIORITY_NORMAL, false, true);

    }
}