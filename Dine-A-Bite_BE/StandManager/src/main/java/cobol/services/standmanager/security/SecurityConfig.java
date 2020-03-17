package cobol.services.standmanager.security;

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
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;


@EnableWebSecurity
@Configuration
@ComponentScan({"cobol.services.standmanager", "cobol.commons"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private DataSource dataSource;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/ping").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults(){
        return new GrantedAuthorityDefaults("");
    }


    @Autowired
    public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


}


