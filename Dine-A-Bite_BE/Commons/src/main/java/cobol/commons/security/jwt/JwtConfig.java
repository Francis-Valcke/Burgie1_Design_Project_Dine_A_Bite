package cobol.commons.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;


@Component
public class JwtConfig {

    @Getter
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey;

    //5 Years
    @Getter
    @Value("${security.jwt.token.secret-key:157680000000}")
    private long validityInMillis;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

}