package cobol.services.authentication.controller;

import lombok.Data;

/**
 * Simple class for automatic parsing of JSON to this object via Jackson
 */
@Data
public class AuthenticationRequest {

    private String username;
    private String password;
}
