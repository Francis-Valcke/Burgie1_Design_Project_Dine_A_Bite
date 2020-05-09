package cobol.commons.stub;

import lombok.extern.log4j.Log4j2;
import okhttp3.Request;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Log4j2
@Service
public class AuthenticationServiceStub extends ServiceStub {

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
}
