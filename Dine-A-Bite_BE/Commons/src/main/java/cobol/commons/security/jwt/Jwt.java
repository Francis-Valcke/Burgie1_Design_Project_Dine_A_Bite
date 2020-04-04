package cobol.commons.security.jwt;

import cobol.commons.security.CommonUser;
import cobol.commons.security.exception.InvalidJwtAuthenticationException;
import io.jsonwebtoken.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public class Jwt {

    private String token;
    private String key;
    private Jws<Claims> claims;

    public Jwt(String token, String key) {
        this.token = token;
        this.key = key;

        this.claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
    }

    /**
     * Retrieving the username from the JWT.
     *
     * @return username
     */
    public String getUsername() {
        return claims
                .getBody()
                .getSubject();
    }

    /**
     * Retrieving the roles from the JWT.
     *
     * @return roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles() {
        return (List<String>) claims
                .getBody()
                .get("roles");
    }

    /**
     * Validating the token based on being tamper free and non-expired.
     *
     * @return valid/invalid
     */
    public boolean validate() throws InvalidJwtAuthenticationException {
        try {
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

    /**
     * Getting authenticated for a provided token.
     *
     * @return Authentication object
     * @throws UsernameNotFoundException UsernameNotFoundException
     */
    public Authentication getAuthentication() throws UsernameNotFoundException {
        UserDetails userDetails;
        userDetails = new CommonUser(getUsername(),getRoles());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * Retrieving the actual token from the HTTP request header.
     *
     * @param req HTTP-request
     * @return JWT
     */
    public static Jwt resolveToken(HttpServletRequest req, String key) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return new Jwt(bearerToken.substring(7), key);
        }
        return null;
    }

    public Jws<Claims> getClaims() {
        return claims;
    }
}
