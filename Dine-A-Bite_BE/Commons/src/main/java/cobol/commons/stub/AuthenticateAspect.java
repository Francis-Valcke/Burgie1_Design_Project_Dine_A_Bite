package cobol.commons.stub;

import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.communication.response.BetterResponseModel;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import sun.security.krb5.Config;

import java.util.Objects;

@Data
@Log4j2
@Aspect
@Component
public class AuthenticateAspect {

    @Autowired
    AuthenticationServiceStub authenticationServiceStub;

    private String username;
    private String password;

    /**
     * Every method annotated with @Authenticated will try to authenticate if not authenticated yet
     */
    @Before("@annotation(cobol.commons.annotation.Authenticated)")
    public void authenticate(){

        try {

            BetterResponseModel<String> response = authenticationServiceStub.authenticate(new AuthenticationRequest(username, password));
            if (response.isOk()) {

                authenticationServiceStub.setAuthorizationToken(Objects.requireNonNull(response).getPayload());
                log.info("Successfully authenticated this module.");

            } else throw response.getException();

        } catch (Throwable throwable) {
            log.error("Could not authenticate.", throwable);
        }

    }
}
