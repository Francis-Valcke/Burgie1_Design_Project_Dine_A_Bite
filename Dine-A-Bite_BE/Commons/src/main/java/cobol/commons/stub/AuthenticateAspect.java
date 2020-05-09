package cobol.commons.stub;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
public class AuthenticateAspect {

    @Before("@annotation(cobol.commons.annotation.Authenticated)")
    public void authenticate(){
        log.info("ASPECT");
    }
}
