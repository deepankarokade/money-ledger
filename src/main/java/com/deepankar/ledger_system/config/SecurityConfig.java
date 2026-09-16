package com.deepankar.ledger_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.deepankar.ledger_system.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                /*
                 * Why STATELESS?
                 * 
                 * JWT authentication means the server doesn't maintain a login session.
                 * 
                 * Traditional session:
                 * 
                 * Client → Login
                 * ↓
                 * Server stores session
                 * ↓
                 * Client → Session ID
                 * 
                 * 
                 * JWT:
                 * 
                 * Client → Login
                 * ↓
                 * JWT returned
                 * ↓
                 * Client → JWT
                 * Client → JWT
                 * Client → JWT
                 * 
                 * The server validates the token on each request.
                 * 
                 * That's the architecture we want for your ledger API.
                 */

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // To keep the register and login endpoint accessible
                        // or even to register or login the account must be authenticated

                        /*
                         * How do I register?
                         * ↓
                         * Need to be authenticated
                         * ↓
                         * How do I authenticate?
                         * ↓
                         * Need to register
                         * ↓
                         * 💀
                         */

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * 
     * For now, we're saying:
     * 
     * Every HTTP request
     * ↓
     * Must be authenticated
     */

    /*
     * Why disable CSRF?
     * .csrf(csrf -> csrf.disable())
     * 
     * Your API is going to be a stateless REST API using JWT rather than browser
     * session authentication, so we'll disable CSRF for this architecture.
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    /*
     * username + password
     * ↓
     * AuthenticationManager
     * ↓
     * UserDetailsService
     * ↓
     * UserRepository
     * ↓
     * users table
     * ↓
     * PasswordEncoder
     * ↓
     * valid / invalid
     */
}