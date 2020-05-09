package cobol.services.authentication;

import lombok.Data;
import org.springframework.security.core.Authentication;

/**
 * Simple class for automatic parsing of JSON to this object via Jackson
 */
@Data
public class AuthenticationRequest {

    public AuthenticationRequest() {}

    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String username;
    private String password;
}
