package cobol.services.authentication.security;

import cobol.commons.security.jwt.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Bean that is in charge of creating tokens.
 * Configured by the JwtConfig bean.
 */
@Service
public class JwtProviderService {

    private JwtConfig jwtConfig;

    /**
     * The service bean method for actually creating JWT's.
     * The settings that are used in this creation method are configured in the JwtConfig class.
     *
     * @param username username of user who requested the token.
     * @param roles roles of that user.
     * @return return JWT token as string.
     */
    public String createToken(String username, List<String> roles) {

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getValidityInMillis());

        return Jwts.builder()//
                .setClaims(claims)//
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecretKey())//
                .compact();
    }

    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }
}
