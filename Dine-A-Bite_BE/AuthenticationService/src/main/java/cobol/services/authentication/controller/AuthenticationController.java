package cobol.services.authentication.controller;

import cobol.commons.ResponseModel;
import cobol.commons.security.exception.DuplicateUserException;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import cobol.services.authentication.security.JwtProviderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cobol.commons.ResponseModel.status.ERROR;
import static cobol.commons.ResponseModel.status.OK;

/**
 * REST api controller for authenticating and creating users.
 */
@RestController
@Log4j2
public class AuthenticationController {

    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtProviderService jwtProviderService;
    private UserRepository users;

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "AuthenticationService is alive!"
     */
    @GetMapping("/pingAS")
    public ResponseEntity ping(HttpServletRequest request) {
        log.debug("Authentication Service was pinged by: " + request.getRemoteAddr());

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("AuthenticationService is alive!")
                        .build().generateResponse()
        );
    }

    /**
     * API endpoint to request authentication for a certain user.
     * This will check the credentials and create a JWT to authenticate the user.
     * The user then uses this token in all forthcoming communication.
     *
     * @param data expects a json body with username and password provided.
     * @return token when login successful.
     */
    @PostMapping("/authenticate")
    public ResponseEntity login(@RequestBody AuthenticationRequest data){
        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            User user = users.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found"));
            List<String> roles = user.getRole();

            String token = jwtProviderService.createToken(username, user.getRole());

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details(model)
                            .build().generateResponse()
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details(e.getLocalizedMessage())
                            .build().generateResponse()
            );

        }
    }

    /**
     * API endpoint to create a new account.
     *
     * @param data expects a json body with username and password provided.
     * @return status op user creation.
     */
    @PostMapping("/create")
    public ResponseEntity create(@RequestBody AuthenticationRequest data){

        try {
            if (users.existsById(data.getUsername()))
                throw new DuplicateUserException("A user with that name exists already.");

            users.save(User.builder()
                            .username(data.getUsername())
                            .password(passwordEncoder.encode(data.getPassword()))
                            .build()
            );

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("User: " + data.getUsername() + " created.")
                            .build().generateResponse()
            );
        } catch (DuplicateUserException e) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details(e.getLocalizedMessage())
                            .build().generateResponse()
            );
        }
    }

    @Autowired
    public void setUsers(UserRepository users) {
        this.users = users;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtProviderService(JwtProviderService jwtProviderService) {
        this.jwtProviderService = jwtProviderService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
