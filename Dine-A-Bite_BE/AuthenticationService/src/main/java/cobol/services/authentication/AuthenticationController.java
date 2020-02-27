package cobol.services.authentication;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class AuthenticationController {

    /**
     * API endpoint to test if the server is still alive.
     * @return
     */
    @GetMapping("/heartbeat")
    public String heartbeat() {
        return "Ok";
    }

    /**
     * API endpoint to request login.
     * If login succeeds, a session token is generated and returned to the client.
     * If not, the client is informed about wrong credentials.
     * @param value
     * @return JWT when authentication is successful.
     */
    @GetMapping("/login")
    public String login(String value){
        String[] credentials = decodeBase64(value);
        return "Username: " + credentials[0] + ", Password: " + credentials[1] + " logged in.";
    }

    /**
     * API endpoint to create a new account.
     * @param value
     * @return
     */
    @GetMapping("/create")
    public String create(String value){
        String[] credentials = decodeBase64(value);
        return "Username: " + credentials[0] + ", Password: " + credentials[1] + " created.";
    }

    /**
     * API endpoint to verify the session validity.
     * @param value
     * @return
     */
    @GetMapping("/verify")
    public String verify(String value){
        String token = decodeBase64(value)[0];
        return "Session Token: " + token;
    }

    /**
     * Decodes from base64 and splits based on the '+' character.
     * @param arg1
     * @return
     */
    private String[] decodeBase64(String arg1){
        String decoded = new String(Base64.getDecoder().decode(arg1));
        return decoded.split("\\+");
    }

}
