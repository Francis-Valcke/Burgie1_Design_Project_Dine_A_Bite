package cobol.services.authentication.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.security.CommonUser;
import cobol.commons.stub.AuthenticationServiceStub;
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
import static cobol.commons.stub.AuthenticationServiceStub.*;

/**
 * REST api controller what requires ROLE_ADMIN or ROLE_USER for access.
 */
@AllArgsConstructor
@RestController
public class UserController {

    private UserRepository userRepository;

    /**
     * API endpoint for retrieving information about the currently authenticated user.
     * The currently authenticated user is the owner of the provided token.
     * @param userDetails easy way for accessing the authenticated user.
     * @return ResponseEntity
     */
    @GetMapping(GET_USER)
    public ResponseEntity<CommonUser> info(@AuthenticationPrincipal CommonUser userDetails){
        User user = userRepository.findById(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername() + " not found!"));

        return ResponseEntity.ok(user.asCommonUser());
    }

    /**
     * API endpoint to allow deletion of users.
     *
     * @param userDetails in the form of a bearer token as an Authorization header.
     *                    Example: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTU4NDExMDYxOSwiZXhwIjoxNTg0MTEwNjc5fQ.arfl_AQUSqmRsBtMgN-NxNRe16NTCgAqzdJxJTkeeh8
     * @return ResponseEntity
     */
    @DeleteMapping(DELETE_USER)
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


    @GetMapping(GET_USER_BALANCE)
    public ResponseEntity<BetterResponseModel<?>> getBalance(@AuthenticationPrincipal CommonUser ap){
        try {
            User user = userRepository.findById(ap.getUsername()).orElseThrow(() -> new DoesNotExistException("The user does not exist, this is not possible"));

            return ResponseEntity.ok(
                    BetterResponseModel.ok(
                            "Payload contains the balance of the user",
                            new BetterResponseModel.GetBalanceResponse(user.getBalance())
                    )
            );

        } catch (DoesNotExistException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }

    }

    @Autowired
    public void setUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
