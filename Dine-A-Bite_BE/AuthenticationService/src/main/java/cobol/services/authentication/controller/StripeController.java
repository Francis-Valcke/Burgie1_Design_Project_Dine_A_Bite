package cobol.services.authentication.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.security.CommonUser;
import cobol.services.authentication.config.ConfigurationBean;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
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

    @GetMapping(value = "/createPaymentIntent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetterResponseModel<?>> createPaymentIntent(double amount) throws StripeException, DoesNotExistException {

        long longAmount = (long)(amount*100);

        Stripe.apiKey = configurationBean.getStripeSecretApiKey();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(longAmount)
                        .setCurrency("eur")
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        BetterResponseModel.CreatePaymentIntentResponse details = new BetterResponseModel.CreatePaymentIntentResponse(
                intent.getClientSecret(),
                configurationBean.getStripePublicApiKey());

        BetterResponseModel<BetterResponseModel.CreatePaymentIntentResponse> response = new BetterResponseModel<>(
                BetterResponseModel.Status.OK,
                details
        );

        return ResponseEntity.ok(response);
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
