package cobol.services.authentication.controller;

import cobol.commons.exception.DoesNotExistException;
import cobol.commons.security.CommonUser;
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

import java.math.BigDecimal;
import java.util.HashMap;

import static cobol.commons.ResponseModel.status.ERROR;
import static cobol.commons.ResponseModel.status.OK;

/**
 * REST api controller what requires ROLE_ADMIN or ROLE_USER for access.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private UserRepository userRepository;

    /**
     * API endpoint for retrieving information about the currently authenticated user.
     * The currently authenticated user is the owner of the provided token.
     * @param userDetails easy way for accessing the authenticated user.
     * @return ResponseEntity
     */
    @GetMapping("")
    public ResponseEntity<CommonUser> info(@AuthenticationPrincipal CommonUser userDetails){
        User user = userRepository.findById(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername() + " not found!"));

        return ResponseEntity.ok(user.asCommonUser());
    }

    @GetMapping("getUser")
    public ResponseEntity<CommonUser> getUser(@RequestParam String username) throws DoesNotExistException {
        return ResponseEntity.ok(userRepository.findById(username).orElseThrow(() -> new DoesNotExistException("The user does not exist.")).asCommonUser());
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
            userRepository.delete(userRepository.findById(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername())));

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

    @GetMapping("/balance")
    public ResponseEntity<HashMap<Object, Object>> getBalance(@AuthenticationPrincipal CommonUser ap){
        try {
            User user = userRepository.findById(ap.getUsername()).orElseThrow(() -> new DoesNotExistException("The user does not exist, this is not possible"));

            return ResponseEntity.ok(
                    ResponseModel.builder()
                    .status(OK)
                    .details(user.getBalance())
                    .build().generateResponse()
            );

        } catch (DoesNotExistException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/balance/add")
    public ResponseEntity<HashMap<Object, Object>> doBalanceAdd(@RequestParam double amount, @AuthenticationPrincipal CommonUser ap){
        try {
            User user = userRepository.findById(ap.getUsername()).orElseThrow(() -> new DoesNotExistException("The user does not exist, this is not possible"));
            user.setBalance(user.getBalance()+(amount));
            userRepository.save(user);

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK)
                            .details(user.getBalance())
                            .build().generateResponse()
            );

        } catch (DoesNotExistException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/balance/subtract")
    public ResponseEntity<HashMap<Object, Object>> doBalanceSubtract(@RequestParam double amount, @AuthenticationPrincipal CommonUser ap){
        try {
            User user = userRepository.findById(ap.getUsername()).orElseThrow(() -> new DoesNotExistException("The user does not exist, this is not possible"));

            double temp = user.getBalance() - amount;

            if (temp < 0) {
                return ResponseEntity.ok(
                        ResponseModel.builder()
                                .status(ERROR)
                                .details("Not enough money!")
                                .build().generateResponse()
                );

            } else {
                user.setBalance(temp);
                userRepository.save(user);

                return ResponseEntity.ok(
                        ResponseModel.builder()
                                .status(OK)
                                .details(user.getBalance())
                                .build().generateResponse()
                );
            }


        } catch (DoesNotExistException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

    }


    @Autowired
    public void setUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
