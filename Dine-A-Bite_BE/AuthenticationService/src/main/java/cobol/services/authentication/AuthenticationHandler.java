package cobol.services.authentication;

import cobol.commons.security.exception.DuplicateUserException;
import cobol.services.authentication.config.ConfigurationBean;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Bean responsible for creating users with certain roles.
 */
@Service
public class AuthenticationHandler {

    private UserRepository userRepository;
    private ConfigurationBean configurationBean;
    private PasswordEncoder passwordEncoder;

    /**
     * Create user with the supplied roles.
     * @param details The details to use for user creation.
     * @param role Roles for the user.
     * @throws StripeException Exception thrown by the Stripe library when trying to create a customer.
     * @throws DuplicateUserException The user already exists.
     */
    public void createUser(AuthenticationRequest details, String... role) throws StripeException, DuplicateUserException {
        if (userRepository.existsById(details.getUsername()))
            throw new DuplicateUserException("A user with that name exists already.");

        // Create an associated stripe customer
        Stripe.apiKey = configurationBean.getStripeSecretApiKey();
        HashMap<String, Object> params = new HashMap<>();
        params.put("description", details.getUsername());
        Customer customer = Customer.create(params);

        userRepository.save(User.builder()
                .username(details.getUsername())
                .password(passwordEncoder.encode(details.getPassword()))
                .customerId(customer.getId())
                .roles(Arrays.asList(role))
                .build()
        );
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setConfigurationBean(ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
