package cobol.services.authentication.config;


import cobol.commons.config.GlobalConfigurationBean;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@ConfigurationProperties(prefix = "authenticationservice")
@Configuration
public class ConfigurationBean {

    private String stripeSecretApiKey;
    private String stripePublicApiKey;

}
