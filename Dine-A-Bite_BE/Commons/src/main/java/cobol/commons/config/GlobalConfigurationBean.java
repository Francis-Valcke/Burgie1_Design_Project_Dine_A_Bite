package cobol.commons.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Setter
@Configuration
@PropertySource(value = {"classpath:global_config.properties"})
@ConfigurationProperties(prefix = "global")
public class GlobalConfigurationBean {

    private boolean local;

    private String localAddressAuthenticationService;
    private String localAddressOrderManager;
    private String localAddressStandManager;
    private String localAddressEventChannel;

    private String remoteAddressAuthenticationService;
    private String remoteAddressOrderManager;
    private String remoteAddressStandManager;
    private String remoteAddressEventChannel;


    public String getAddressAuthenticationService() {
        return local ? localAddressAuthenticationService : remoteAddressAuthenticationService;
    }

    public String getAddressOrderManager() {
        return local ? localAddressOrderManager : remoteAddressOrderManager;
    }

    public String getAddressStandManager() {
        return local ? localAddressStandManager : remoteAddressStandManager;
    }

    public String getAddressEventChannel() {
        return local ? localAddressEventChannel : remoteAddressEventChannel;
    }
}