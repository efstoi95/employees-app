package com.enterprise.employees.security;
import com.enterprise.employees.repository.EmployeesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Optional;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig  {

    private final EmployeesRepository employeeRepository;
    /**
     * Creates and returns a UserDetailsService bean.
     *
     * @return  a UserDetailsService bean
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username ->
                Optional.ofNullable(employeeRepository.findByUsername(username))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }



    /**
     * Configures the WebSecurityCustomizer to ignore requests to the "/h2-console/**" URL pattern.
     *
     * @return the WebSecurityCustomizer bean
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**");
    }

    /**
     * Creates a delegating password encoder that supports multiple encoding formats.
     *
     * @return the PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.
                createDelegatingPasswordEncoder();
    }
    /**
     * Configures the SecurityFilterChain for the HttpSecurity object.
     *
     * @param  http  the HttpSecurity object to configure
     * @return       the SecurityFilterChain object
     * @throws Exception if an error occurs during the configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.
                // Configure authorization requests
                authorizeHttpRequests(authz -> authz
                        // Allow all requests to URLs under "/web/**"
                        .requestMatchers("/web/**").permitAll()
                        .requestMatchers("/posts/uploadImage").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
        )// Configure form login
        .formLogin(authz -> authz
                // Use a custom login page
                .loginPage("/login").permitAll()
        )// Configure logout
        .logout(authz -> authz
                // Delete the JSESSIONID cookie upon logout
                .deleteCookies("JSESSIONID")
                // Match logout requests to the URL "/logout"
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                // Redirect to the login page with a logout parameter upon successful logout
                .logoutSuccessUrl("/login?logout")
            )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/posts/uploadImage")
                );


        return http.build();
    }
    /**
     * Creates and configures an instance of AuthenticationProvider.
     *
     * @return  an instance of AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // Set the UserDetailsService for retrieving user details
        authenticationProvider.setUserDetailsService(userDetailsService());
        // Set the PasswordEncoder for encoding and verifying passwords
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        // Return the configured AuthenticationProvider
        return authenticationProvider;
    }


}


