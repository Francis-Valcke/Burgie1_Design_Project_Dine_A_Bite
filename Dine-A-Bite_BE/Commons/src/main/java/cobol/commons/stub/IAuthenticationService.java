package cobol.commons.stub;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.domain.CommonUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;

public interface IAuthenticationService {


    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "AuthenticationService is alive!"
     */

    ResponseEntity<HashMap<Object,Object>> ping(HttpServletRequest request);

    /**
     * API endpoint to request authentication for a certain user.
     * This will check the credentials and create a JWT to authenticate the user.
     * The user then uses this token in all forthcoming communication.
     *
     * @param data expects a json body with username and password provided.
     * @return token when login successful.
     */
    ResponseEntity<HashMap<Object,Object>> authenticate(AuthenticationRequest data);

    /**
     * API endpoint to create a new account.
     *
     * @param details expects a json body with username and password provided.
     * @return status op user creation.
     */
    ResponseEntity<HashMap<Object,Object>> create(AuthenticationRequest details);

    /**
     * API endpoint to create a new account.
     *
     * @param data expects a json body with username and password provided.
     * @return status op user creation.
     */
    ResponseEntity<HashMap<Object,Object>> createStandManager(AuthenticationRequest data);

    /**
     * Temporary API endpoint for testing role security.
     *
     * @return ResponseEntity
     */
    ResponseEntity verifyAdminTest();

    ResponseEntity<BetterResponseModel<?>> getEphemeralKey(String version, CommonUser user);

    ResponseEntity<BetterResponseModel<?>> createPaymentIntent(String amount, CommonUser user);

    ResponseEntity<BetterResponseModel<?>> createTransaction(BigDecimal amount, String otherUser, CommonUser user);

    ResponseEntity<BetterResponseModel<?>> confirmTransaction(String otherUser, CommonUser user);

    ResponseEntity<BetterResponseModel<?>> cancelTransaction(String otherUser, CommonUser user);

    /**
     * API endpoint for retrieving information about the currently authenticated user.
     * The currently authenticated user is the owner of the provided token.
     * @param userDetails easy way for accessing the authenticated user.
     * @return ResponseEntity
     */
    ResponseEntity<CommonUser> getUserInfo(CommonUser userDetails);

    /**
     * API endpoint to allow deletion of users.
     *
     * @param userDetails in the form of a bearer token as an Authorization header.
     *                    Example: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTU4NDExMDYxOSwiZXhwIjoxNTg0MTEwNjc5fQ.arfl_AQUSqmRsBtMgN-NxNRe16NTCgAqzdJxJTkeeh8
     * @return ResponseEntity
     */
    ResponseEntity deleteUser(UserDetails userDetails);

    ResponseEntity<BetterResponseModel<?>> getBalance(CommonUser ap);
}
