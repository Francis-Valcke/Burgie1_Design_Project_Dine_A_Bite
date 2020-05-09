package cobol.commons.stub;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.domain.CommonUser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

@Log4j2
@Service
public class AuthenticationServiceStub extends ServiceStub implements IAuthenticationService{

    // AuthenticationController
    public static final String GET_PING = "/ping";
    public static final String POST_AUTHENTICATE = "/authenticate";
    public static final String POST_CREATE_USER = "/createUser";
    public static final String POST_CREATE_STAND_MANAGER = "/createStandManager";

    // UserController
    public static final String GET_USER = "/user";
    public static final String DELETE_USER = "/user";
    public static final String GET_USER_BALANCE = "/user/balance";

    // StripeController
    public static final String GET_STRIPE_KEY = "/stripe/key";
    public static final String POST_STRIPE_CREATE_PAYMENT_INTENT = "/stripe/createPaymentIntent";
    public static final String POST_STRIPE_CREATE_TRANSACTION = "/stripe/createTransaction";
    public static final String GET_STRIPE_CONFIRM_TRANSACTION = "/stripe/confirmTransaction";
    public static final String GET_STRIPE_CANCEL_TRANSACTION = "/stripe/cancelTransaction";

    // AdminController
    public static final String GET_ADMIN_INFO = "/admin";

    @Override
    public String getAddress(){
        return globalConfigurationBean.getAddressAuthenticationService();
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> ping(HttpServletRequest request) {
        return null;
    }

    @Override
    public BetterResponseModel<String> authenticate(AuthenticationRequest data) throws IOException {

        String uri = getAddress() + POST_AUTHENTICATE;

        String string = objectMapper.writeValueAsString(data);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, string);
        //RequestBody body = RequestBody.create(string, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(uri)
                .method("POST", body)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        BetterResponseModel<String> toReturn =
                objectMapper.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<BetterResponseModel<String>>() {});

        response.close();

        return toReturn;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> create(AuthenticationRequest details) {
        return null;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> createStandManager(AuthenticationRequest data) {
        return null;
    }

    @Override
    public ResponseEntity verifyAdminTest() {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> getEphemeralKey(String version, CommonUser user) {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> createPaymentIntent(String amount, CommonUser user) {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> createTransaction(BigDecimal amount, String otherUser, CommonUser user) {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> confirmTransaction(String otherUser, CommonUser user) {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> cancelTransaction(String otherUser, CommonUser user) {
        return null;
    }

    @Override
    public ResponseEntity<CommonUser> getUserInfo(CommonUser userDetails) {
        return null;
    }

    @Override
    public ResponseEntity deleteUser(UserDetails userDetails) {
        return null;
    }

    @Override
    public ResponseEntity<BetterResponseModel<?>> getBalance(CommonUser ap) {
        return null;
    }
}
