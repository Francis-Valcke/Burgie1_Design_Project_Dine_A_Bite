package cobol.services.authentication.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.BetterResponseModel.CreatePaymentIntentResponse;
import cobol.commons.BetterResponseModel.GetBalanceResponse;
import cobol.commons.BetterResponseModel.Status;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.security.CommonUser;
import cobol.commons.security.Role;
import cobol.services.authentication.config.ConfigurationBean;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import cobol.services.authentication.exception.NotAuthorizedException;
import cobol.services.authentication.exception.NotEnoughMoneyException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.EphemeralKey;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RestController responsible for providing payment related endpoints.
 */
@RestController
@RequestMapping("/stripe")
public class StripeController {

    UserRepository userRepository;
    ConfigurationBean configurationBean;

    /**
     * The first step in initiating a transaction is getting an ephemeral key from Stripe's backend.
     * This needs to be returned to the client.
     * @param version The client's version.
     * @param user The authenticated user.
     * @return ephemeral key.
     */
    @GetMapping(value = "/key", produces = MediaType.APPLICATION_JSON_VALUE)
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

    /**
     * With the ephemeral key the client can now request the creation of a payment intent, which is the first phase in
     * the two step process for payments.
     * @param amount The payment amount, currently fixed in euro as currency.
     * @param user The authenticated user on which to perform the transaction.
     * @return intent
     */
    @PostMapping(value = "/createPaymentIntent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createPaymentIntent(String amount, @AuthenticationPrincipal CommonUser user) {

        try {

            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            BigDecimal decimalAmount = new BigDecimal(amount);

            // First try to create a transaction
            ResponseEntity<BetterResponseModel<?>> transaction = createTransaction(decimalAmount, null, user);
            if (Objects.requireNonNull(transaction.getBody()).getStatus().equals(Status.ERROR)) {
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

            CreatePaymentIntentResponse details = new CreatePaymentIntentResponse(
                    intent.getClientSecret(),
                    configurationBean.getStripePublicApiKey()
            );

            return ResponseEntity.ok(BetterResponseModel.ok("Stripe payment intent created!", details));

        } catch (Throwable e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    /**
     * Both a method and a API endpoint to be used by our backend for initiation a two phase transaction for a certain
     * amount. For adding money the amount should be a positive decimal, for subtracting money, the amount should be
     * negative. There is a check in place to make sure the user has enough money for the transaction. The amount will
     * placed in an unconfirmed payment field of the user entity such that the transaction will either go through on
     * a confirmation or get changed back to 0 when the transaction is cancelled.
     *
     * @param amount The transaction amount in euro's.
     * @param otherUser If this is called by another module such as the OrderManager the authenticated user will be the
     *                  OrderManager itself, in that case, otherUser contains the user to perform the transaction on.
     *                  This will only be allowed if this is called though a user with the APPLICATION role in the
     *                  system so that this cannot be used by other parties to increase their own balance.
     * @param user The currently authenticated user.
     * @return success or fail
     */
    @PostMapping(value = "/createTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createTransaction(BigDecimal amount, @RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {

        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)){
                throw new NotAuthorizedException("User: " + user.getUsername() + " is not authorized to perform this action.");
            }

            User userEntity = userRepository.findById(otherUser == null ? user.getUsername() : otherUser)
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0){
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            // Set the unconfirmedPayment
            userEntity.setUnconfirmedPayment(amount);
            userRepository.save(userEntity);

            GetBalanceResponse details = new GetBalanceResponse(userEntity.getBalance());

            return ResponseEntity.ok(BetterResponseModel.ok("Created the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException | NotAuthorizedException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }

    }

    /**
     * The second phase in the two phase transaction. Confirming the transaction will calculate the new balance based on
     * the value that is stored in the unconfirmed transaction field of the user.
     * @param otherUser If this is called by another module such as the OrderManager the authenticated user will be the
     *                  OrderManager itself, in that case, otherUser contains the user to perform the transaction on.
     *                  This will only be allowed if this is called though a user with the APPLICATION role in the
     *                  system so that this cannot be used by other parties to increase their own balance.
     * @param user The currently authenticated user.
     * @return success or fail
     */
    @GetMapping(value = "/confirmTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> confirmTransaction(@RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {
        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)){
                throw new NotAuthorizedException("User: " + user.getUsername() + " is not authorized to perform this action.");
            }

            User userEntity = userRepository.findById(otherUser == null ? user.getUsername() : otherUser)
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check again if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance().add(userEntity.getUnconfirmedPayment()).compareTo(BigDecimal.ZERO) < 0){
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            userEntity.setBalance(userEntity.getBalance().add(userEntity.getUnconfirmedPayment()));
            userEntity.setUnconfirmedPayment(BigDecimal.ZERO);
            userRepository.save(userEntity);

            GetBalanceResponse details = new GetBalanceResponse(userEntity.getBalance());
            return ResponseEntity.ok(BetterResponseModel.ok("Confirmed the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException | NotAuthorizedException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    /**
     * This method will cancell the transaction by setting the unconfirmed payment field of the user back to zero.
     * @param otherUser If this is called by another module such as the OrderManager the authenticated user will be the
     *                  OrderManager itself, in that case, otherUser contains the user to perform the transaction on.
     *                  This will only be allowed if this is called though a user with the APPLICATION role in the
     *                  system so that this cannot be used by other parties to increase their own balance.
     * @param user The currently authenticated user.
     * @return success or fail
     */
    @GetMapping(value = "/cancelTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> cancelTransaction(@RequestParam(value = "user", required = false) String otherUser, @AuthenticationPrincipal CommonUser user) {
        try {

            // When otherUser is specified, check if the authenticationPrincipal is our application that issued the request
            if (otherUser != null && !user.getRoles().contains(Role.APPLICATION)){
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

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setConfigurationBean(ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }
}
