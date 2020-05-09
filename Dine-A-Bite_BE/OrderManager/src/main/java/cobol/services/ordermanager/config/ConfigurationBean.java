package cobol.services.ordermanager.config;


import cobol.commons.stub.Action;
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

@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "ordermanager")
public class ConfigurationBean {

    @Autowired
    MenuHandler menuHandler;
    @Autowired
    StandManagerStub standManagerStub;

    boolean unitTest;

    @PostConstruct
    public void postConstruct(){
        standManagerStub.doOnAvailable(() -> {
            try {
                menuHandler.updateStandManager();
                log.info("Stand manager has been updated successfully");
            } catch (JsonProcessingException e) {
                log.error("Could not update stand manager after becoming available again.", e);
            }
        });
    }

}
