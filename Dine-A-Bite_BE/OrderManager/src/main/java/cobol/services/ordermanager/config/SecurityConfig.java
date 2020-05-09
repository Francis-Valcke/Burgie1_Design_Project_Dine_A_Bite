package cobol.services.ordermanager.config;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

/**
 * Configuration class for configuring Spring Security filters.
 * Assigning authentication datasource.
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private DataSource dataSource;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetailsService userDetailsService;

    /**
     * Overridden method for configuring the AuthenticationManagerBuilder.
     * Here we indicate to the builder that in memory authentication scheme is used.
     * This is not entirely accurate as we are going to be authenticating solely on the presence of a valid JWT.
     *
     * @param auth to be configured
     * @throws Exception any kind of exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication();
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
                //Requests that should not be authenticated
                .antMatchers("/ping").permitAll()
                //Permissions for the DBController
                .antMatchers("/db/*").hasRole(Role.ADMIN)
                //.antMatchers("/db/*").permitAll()
                //Permissions for StandController
                .antMatchers("/verify", "/addStand", "/updateStand", "/deleteStand").hasRole(Role.STAND)
                //Others
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

    @Autowired
    public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


}


