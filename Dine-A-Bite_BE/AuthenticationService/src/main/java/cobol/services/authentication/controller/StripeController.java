package cobol.services.authentication.controller;

import cobol.commons.security.CommonUser;
import com.stripe.exception.StripeException;
import com.stripe.model.EphemeralKey;
import com.stripe.net.RequestOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StripeController {

    @GetMapping
    public ResponseEntity<EphemeralKey> getEphemeralKey(@RequestParam String version, @AuthenticationPrincipal CommonUser user) throws StripeException {

        RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder())
                .setStripeVersionOverride(version)
                //.setStripeVersion("{{API_VERSION}}")
                .build();
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("customer", user.getUsername());
        EphemeralKey key = EphemeralKey.create(options, requestOptions);

        return ResponseEntity.ok(key);
    }
}
