package cobol.services.ordermanager.config;


import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.stub.Action;
import cobol.commons.stub.AuthenticationServiceStub;
import cobol.commons.stub.EventChannelStub;
import cobol.commons.stub.StandManagerStub;
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
    MenuHandler menuHandler;
    @Autowired
    AuthenticationServiceStub authenticationServiceStub;
    @Autowired
    OrderProcessor orderProcessor;

    private String username;
    private String password;
    boolean unitTest;

    private boolean authenticated = false;
    private boolean subscribed = false;

    /**
     * When the event channel becomes available, try to retrieve a subscriberID
     */
    @PostConstruct
    private void getSubscriberId() {

        eventChannelStub.doOnAvailable(() -> {
            try {
                int id = eventChannelStub.getRegisterSubscriber("");
                orderProcessor.setSubscriberId(id);
                subscribed = true;

                log.info("Successfully requested a subscriber ID from event channel: ID = " + id);
            } catch (Exception e) {
                subscribed = false;
                log.error("Could not request subscriber ID from event channel.", e);
            }
        }, Action.PRIORITY_NORMAL, false);

        eventChannelStub.doOnUnavailable(()->{

            subscribed = false;

        }, Action.PRIORITY_NORMAL, false);
    }

    /**
     * When the stand manager becomes available, try to update its schedulers
     */
    @PostConstruct
    public void updateStandManager(){
        standManagerStub.doOnAvailable(() -> {
            try {
                menuHandler.updateStandManager();
                log.info("Stand manager has been updated successfully");
            } catch (JsonProcessingException e) {
                log.error("Could not update stand manager after becoming available again.", e);
            }
        }, Action.PRIORITY_NORMAL, false);
    }
}