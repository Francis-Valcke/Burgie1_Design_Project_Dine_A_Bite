package cobol.commons.stub;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

@Aspect
@Log4j2
@Data
@Configuration
@ConfigurationProperties("credentials")
@Scope(value = "singleton")
public class AuthenticationHandler {

    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    // This will hold the authorization token
    private String token;

    // These values will be pulled from the application.yml files
    private String username;
    private String password;

    /**
     * Every method annotated with @Authenticated will try to authenticate if not authenticated yet
     */
    @Before("@annotation(cobol.commons.annotation.Authenticated)")
    public void authenticateAdvice() throws Throwable {

        if (!isAuthenticated()) {

            log.info("Trying to authenticate with credentials: " + username + " + " + password);

            try {

                BetterResponseModel<String> response = authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
                if (response.isOk()) {

                    token = "Bearer " + Objects.requireNonNull(response).getPayload();
                    log.info("Successfully authenticated this module.");

                } else throw response.getException();

            } catch (Throwable throwable) {
                log.info("Could not authenticate.");
                log.debug("Could not authenticate.", throwable);
            }

        }

    }

    public boolean isAuthenticated(){
        return token!=null && !token.isEmpty();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = "Bearer " + token;
    }

}
