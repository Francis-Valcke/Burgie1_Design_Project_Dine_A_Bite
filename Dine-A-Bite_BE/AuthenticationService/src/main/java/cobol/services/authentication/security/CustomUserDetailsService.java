package cobol.services.authentication.security;

import cobol.services.authentication.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    /**
     * This overridden method is used by the authentication manager to retrieve a UserDetails object
     * from a given username. How to UserDetails object is retrieved is up to the programmer.
     * Here it is retrieved from a database.
     *
     * @param username user to be fetched from the database
     * @return UserDetails object
     * @throws UsernameNotFoundException User not found in database/
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.users.findById(username)
            .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));
    }
}
