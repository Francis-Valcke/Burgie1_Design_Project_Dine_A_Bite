package cobol.services.authentication.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ConfigurationBean {

    private String stripeSecretApiKey;
    private String stripePublicApiKey;

}
