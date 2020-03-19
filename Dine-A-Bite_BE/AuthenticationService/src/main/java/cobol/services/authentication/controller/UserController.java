package cobol.services.authentication.controller;

import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import cobol.commons.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import static cobol.commons.ResponseModel.status.OK;

/**
 * REST api controller what requires ROLE_ADMIN or ROLE_USER for access.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private UserRepository users;

    /**
     * API endpoint for retrieving information about the currently authenticated user.
     * The currently authenticated user is the owner of the provided token.
     * @param userDetails easy way for accessing the authenticated user.
     * @return ResponseEntity
     */
    @GetMapping("")
    public ResponseEntity info(@AuthenticationPrincipal UserDetails userDetails){
        User user = users.findById(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername() + " not found!"));

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details(user)
                        .build().generateResponse()
        );
    }

    /**
     * API endpoint to allow deletion of users.
     *
     * @param userDetails in the form of a bearer token as an Authorization header.
     *                    Example: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTU4NDExMDYxOSwiZXhwIjoxNTg0MTEwNjc5fQ.arfl_AQUSqmRsBtMgN-NxNRe16NTCgAqzdJxJTkeeh8
     * @return ResponseEntity
     */
    @DeleteMapping("")
    public ResponseEntity delete(@AuthenticationPrincipal UserDetails userDetails){
        try {
            users.delete(users.findById(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername())));

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("User: " + userDetails.getUsername() + " has been removed.")
                            .build().generateResponse()
            );
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details(e.getLocalizedMessage())
                            .build().generateResponse()
            );
        }


    }

    @Autowired
    public void setUsers(UserRepository users) {
        this.users = users;
    }
}
