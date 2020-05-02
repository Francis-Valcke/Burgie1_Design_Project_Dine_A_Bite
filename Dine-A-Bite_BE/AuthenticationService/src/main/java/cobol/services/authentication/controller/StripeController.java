package cobol.services.authentication.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.BetterResponseModel.*;
import cobol.commons.ResponseModel;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.security.CommonUser;
import cobol.services.authentication.config.ConfigurationBean;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import cobol.services.ordermanager.exception.NotEnoughMoneyException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cobol.commons.ResponseModel.status.ERROR;
import static cobol.commons.ResponseModel.status.OK;


@RestController
@RequestMapping("/stripe")
public class StripeController {

    UserRepository userRepository;
    ConfigurationBean configurationBean;

    @GetMapping(value = "/key", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEphemeralKey(@RequestParam("api_version") String version, @AuthenticationPrincipal CommonUser user) throws StripeException, DoesNotExistException {

        User userEntity = userRepository.findById(user.getUsername())
                .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

        RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder())
                .setStripeVersionOverride("2020-03-02")
                .setApiKey("sk_test_gd0OhhbEHENmKoo0HRhUYX1r00P0pmGByO")
                //.setStripeVersion("{{API_VERSION}}")
                .build();
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("customer", userEntity.getCustomerId());
        EphemeralKey key = EphemeralKey.create(options, requestOptions);

        return key.getRawJson();
    }

    @PostMapping(value = "/createPaymentIntent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createPaymentIntent(double amount, @AuthenticationPrincipal CommonUser user) {

        try {

            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // First try to create a transaction
            ResponseEntity<BetterResponseModel<?>> transaction = createTransaction(amount, user);
            if (Objects.requireNonNull(transaction.getBody()).getStatus().equals(Status.ERROR)) {
                // If there was an error creating the transaction, throw exception
                throw transaction.getBody().getException();
            }

            // Then handle stripe
            long longAmount = (long) (amount * 100);

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


    @PostMapping(value = "/createTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createTransaction(double amount, @AuthenticationPrincipal CommonUser user) {

        try {
            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance()+amount <= 0){
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            // Set the unconfirmedPayment
            userEntity.setUnconfirmedPayment(amount);
            userRepository.save(userEntity);

            GetBalanceResponse details = new GetBalanceResponse(userEntity.getBalance());

            return ResponseEntity.ok(BetterResponseModel.ok("Created the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }

    }

    @GetMapping(value = "/confirmTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> confirmTransaction(@AuthenticationPrincipal CommonUser user) {
        try {

            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            // Check again if the transaction could go through ie remaining balance >=0
            if (userEntity.getBalance()+userEntity.getUnconfirmedPayment() <= 0){
                throw new NotEnoughMoneyException("There is not enough money on the account for this transaction.");
            }

            userEntity.setBalance(userEntity.getBalance() + userEntity.getUnconfirmedPayment());
            userEntity.setUnconfirmedPayment(0);
            userRepository.save(userEntity);

            GetBalanceResponse details = new GetBalanceResponse(userEntity.getBalance());
            return ResponseEntity.ok(BetterResponseModel.ok("Confirmed the transaction.", details));

        } catch (DoesNotExistException | NotEnoughMoneyException e) {
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
    }

    @GetMapping(value = "/cancelTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> cancelTransaction(@AuthenticationPrincipal CommonUser user) {
        try {

            User userEntity = userRepository.findById(user.getUsername())
                    .orElseThrow(() -> new DoesNotExistException("This user does not exist in the database. This should not be possible!"));

            userEntity.setUnconfirmedPayment(0);
            userRepository.save(userEntity);

            BetterResponseModel.GetBalanceResponse details = new BetterResponseModel.GetBalanceResponse(userEntity.getBalance());

            return ResponseEntity.ok(BetterResponseModel.ok("Cancelled the transaction.", details));

        } catch (DoesNotExistException e) {
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
