package cobol.commons.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Custom authentication filter added to spring security that uses JWT's for authentication.
 *
 */
@Component
public class JwtAuthenticationFilter extends GenericFilterBean {

    private JwtVerificationService jwtVerificationService;


    /**
     * Custom filter to verify the provided JWT.
     *
     * @param req ServletRequest
     * @param res ServletResponse
     * @param filterChain FilterChain
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {


            String token = jwtVerificationService.resolveToken((HttpServletRequest) req);
            if (token != null && jwtVerificationService.validateToken(token)) {
                Authentication auth = jwtVerificationService.getAuthentication(token);

                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            filterChain.doFilter(req, res);

    }

    @Autowired
    public void setJwtVerificationService(JwtVerificationService jwtVerificationService) {
        this.jwtVerificationService = jwtVerificationService;
    }
}