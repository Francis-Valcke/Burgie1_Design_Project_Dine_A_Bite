package cobol.commons.security.jwt;

import cobol.commons.security.CommonUser;
import cobol.commons.security.exception.InvalidJwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class JwtVerificationService {

    private JwtConfig jwtConfig;

    public Authentication getAuthentication(String token) throws UsernameNotFoundException {
        UserDetails userDetails = null;
        userDetails = CommonUser.builder()
                .username(getUsername(token))
                .role(getRoles(token))
                .build();
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

    private String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @SuppressWarnings("unchecked")
    private List<String> getRoles(String token) {
        return (List<String>) Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
    }

    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

}
