package cobol.services.authentication.controller;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.communication.response.ResponseModel;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.domain.CommonUser;
import cobol.commons.security.Role;
import cobol.commons.security.exception.DuplicateUserException;
import cobol.commons.stub.IAuthenticationService;
import cobol.services.authentication.AuthenticationHandler;
import cobol.commons.communication.requst.AuthenticationRequest;
import cobol.commons.stub.AuthenticationServiceStub;
import cobol.services.authentication.config.ConfigurationBean;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import cobol.services.authentication.exception.NotAuthorizedException;
import cobol.services.authentication.exception.NotEnoughMoneyException;
import cobol.services.authentication.security.JwtProviderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.EphemeralKey;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import static cobol.commons.communication.response.ResponseModel.status.ERROR;
import static cobol.commons.communication.response.ResponseModel.status.OK;
import static cobol.commons.stub.AuthenticationServiceStub.*;

/**
 * REST api controller for authenticating and creating users.
 */
@RestController
@Log4j2
public class AuthenticationServiceController implements IAuthenticationService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProviderService jwtProviderService;
    @Autowired
    private ConfigurationBean configurationBean;
    @Autowired
    private AuthenticationHandler authenticationHandler;
    @Autowired
    private UserRepository userRepository;

    @Override
    @GetMapping(AuthenticationServiceStub.GET_PING)
    public ResponseEntity<HashMap<Object, Object>> ping(HttpServletRequest request) {
        log.debug("Authentication Service was pinged by: " + request.getRemoteAddr());

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("AuthenticationService is alive!")
                        .build().generateResponse()
        );
    }

    @Override
    @PostMapping(AuthenticationServiceStub.POST_AUTHENTICATE)
    public BetterResponseModel<String> authenticate(@RequestBody AuthenticationRequest data) {
        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            User user = userRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found"));

            String token = jwtProviderService.createToken(username, user.getRoles());


            return BetterResponseModel.ok(
                            "Authentication success.",
                            token
                    );

        } catch (AuthenticationException e) {

            return BetterResponseModel.error(
                            "Authentication failed.",
                            e

                    );
        }
    }

    @Override
    @PostMapping(AuthenticationServiceStub.POST_CREATE_USER)
    public ResponseEntity<HashMap<Object, Object>> create(@RequestBody AuthenticationRequest details) {

        try {

            authenticationHandler.createUser(details, Role.USER);

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("User: " + details.getUsername() + " created.")
                            .build().generateResponse()
            );
        } catch (DuplicateUserException | StripeException e) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details(e.getLocalizedMessage())
                            .build().generateResponse()
            );
        }
    }

    @Override
    @PostMapping(AuthenticationServiceStub.POST_CREATE_STAND_MANAGER)
    public ResponseEntity<HashMap<Object, Object>> createStandManager(@RequestBody AuthenticationRequest data) {

        try {
            authenticationHandler.createUser(data, Role.USER, Role.STAND);

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("Stand Manager: " + data.getUsername() + " created.")
                            .build().generateResponse()
            );
        } catch (DuplicateUserException | StripeException e) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details(e.getLocalizedMessage())
                            .build().generateResponse()
            );
        }
    }


    @Override
    @GetMapping(AuthenticationServiceStub.GET_ADMIN_INFO)
    public ResponseEntity verifyAdminTest() {

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("Welcome to the admin page")
                        .build().generateResponse()
        );
    }

    @Override
    @GetMapping(value = GET_STRIPE_KEY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> getEphemeralKey(@RequestParam("api_version") String version, @AuthenticationPrincipal CommonUser user) {

        try {
            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder())
                    .setStripeVersionOverride(version)
                    .setApiKey(configurationBean.getStripeSecretApiKey())
                    .build();
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("customer", userEntity.getCustomerId());
            EphemeralKey key = EphemeralKey.create(options, requestOptions);

            return ResponseEntity.ok(
                    BetterResponseModel.ok(
                            "The payload contains the ephemeral key in json. The entire payload needs to be passed ephemeralKeyUpdateListener.onKeyUpdate()",
                            key.getRawJson()
                    )
            );
        } catch (DoesNotExistException | StripeException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    @Override
    @PostMapping(value = POST_STRIPE_CREATE_PAYMENT_INTENT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createPaymentIntent(String amount, @AuthenticationPrincipal CommonUser user) {

        try {

            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            BigDecimal decimalAmount = new BigDecimal(amount);

            // First try to create a transaction
            ResponseEntity<BetterResponseModel<?>> transaction = createTransaction(decimalAmount, null, user);
            if (Objects.requireNonNull(transaction.getBody()).getStatus().equals(BetterResponseModel.Status.ERROR)) {
                // If there was an error creating the transaction, throw exception
                throw transaction.getBody().getException();
            }

            // Then handle stripe
            long longAmount = Long.parseLong(amount) * 100;

            Stripe.apiKey = configurationBean.getStripeSecretApiKey();

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(longAmount)
                            .setCurrency("eur")
                            .setCustomer(userEntity.getCustomerId())
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            BetterResponseModel.CreatePaymentIntentResponse details = new BetterResponseModel.CreatePaymentIntentResponse(
                    intent.getClientSecret(),
                    configurationBean.getStripePublicApiKey()
            );

            return ResponseEntity.ok(BetterResponseModel.ok("Stripe payment intent created!", details));

        } catch (Throwable e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    @Override
    @PostMapping(value = POST_STRIPE_CREATE_TRANSACTION, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createTransaction(BigDecimal amount, @RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {

        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)) {
                throw new NotAuthorizedException("User: " + user.getUsername() + " is not authorized to perform this action.");
            }

            User userEntity = userRepository.findById(otherUser == null ? user.getUsername() : otherUser)
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            // Set the unconfirmedPayment
            userEntity.setUnconfirmedPayment(amount);
            userRepository.save(userEntity);

            BetterResponseModel.GetBalanceResponse details = new BetterResponseModel.GetBalanceResponse(userEntity.getBalance());

            return ResponseEntity.ok(BetterResponseModel.ok("Created the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException | NotAuthorizedException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }

    }

    @Override
    @GetMapping(value = GET_STRIPE_CONFIRM_TRANSACTION, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> confirmTransaction(@RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {
        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)) {
                throw new NotAuthorizedException("User: " + user.getUsername() + " is not authorized to perform this action.");
            }

            User userEntity = userRepository.findById(otherUser == null ? user.getUsername() : otherUser)
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check again if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance().add(userEntity.getUnconfirmedPayment()).compareTo(BigDecimal.ZERO) < 0) {
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            userEntity.setBalance(userEntity.getBalance().add(userEntity.getUnconfirmedPayment()));
            userEntity.setUnconfirmedPayment(BigDecimal.ZERO);
            userRepository.save(userEntity);

            BetterResponseModel.GetBalanceResponse details = new BetterResponseModel.GetBalanceResponse(userEntity.getBalance());
            return ResponseEntity.ok(BetterResponseModel.ok("Confirmed the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException | NotAuthorizedException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    @Override
    @GetMapping(value = GET_STRIPE_CANCEL_TRANSACTION, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> cancelTransaction(@RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {
        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)) {
                throw new NotAuthorizedException("User: " + user.getUsername() + " is not authorized to perform this action.");
            }

            User userEntity = userRepository.findById(otherUser == null ? user.getUsername() : otherUser)
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            userEntity.setUnconfirmedPayment(BigDecimal.ZERO);
            userRepository.save(userEntity);

            BetterResponseModel.GetBalanceResponse details = new BetterResponseModel.GetBalanceResponse(userEntity.getBalance());

            return ResponseEntity.ok(BetterResponseModel.ok("Cancelled the transaction.", details));

        } catch (DoesNotExistException | NotAuthorizedException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    @Override
    @GetMapping(GET_USER)
    public ResponseEntity<CommonUser> getUserInfo(@AuthenticationPrincipal CommonUser userDetails) {
        User user = userRepository.findById(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername() + " not found!"));

        return ResponseEntity.ok(user.asCommonUser());
    }

    @Override
    @DeleteMapping(DELETE_USER)
    public ResponseEntity deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
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

    @Override
    @GetMapping(GET_USER_BALANCE)
    public ResponseEntity<BetterResponseModel<?>> getBalance(@AuthenticationPrincipal CommonUser ap) {
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

}
