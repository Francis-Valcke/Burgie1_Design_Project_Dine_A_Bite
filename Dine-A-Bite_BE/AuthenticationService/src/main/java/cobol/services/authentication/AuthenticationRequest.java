package cobol.services.authentication;

import lombok.Data;
import org.springframework.security.core.Authentication;

/**
 * Data class for representing authentication requests.
 * Used for creating users and authenticating.
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
