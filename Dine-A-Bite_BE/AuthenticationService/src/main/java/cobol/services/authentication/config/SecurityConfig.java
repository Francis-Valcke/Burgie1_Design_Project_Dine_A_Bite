package cobol.services.authentication.config;

import cobol.commons.security.Role;
import cobol.commons.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;


/**
 * Configuration class for configuring Spring Security filters.
 * Assigning authentication datasource.
 */
@EnableWebSecurity
@Configuration
@ComponentScan({"cobol.services.authentication", "cobol.commons"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;


    /**
     * Overridden method for configuring the AuthenticationManagerBuilder.
     * Here we indicate to the builder that a custom userDetailsService is being used which is injected into this class.
     *
     * @param auth to be configured
     * @throws Exception any kind of exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }


    /**
     * Overridden method for indicating to Spring Security which
     * kind of security is requested and for which API endpoints.
     *
     * @param http to be configured
     * @throws Exception any kind of exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/admin").hasRole(Role.ADMIN)
                .antMatchers("/user").hasAnyRole(Role.ADMIN, Role.USER)
                .antMatchers("/authenticate").permitAll()
                .antMatchers("/createUser").permitAll()
                .antMatchers("/createStandManager").permitAll()
                .antMatchers("/pingAS").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }

    /**
     * Easy way of exposing the AuthenticationManager as a bean.
     *
     * @return AuthenticationManager
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Resource(name="customUserDetailsService")
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
}


