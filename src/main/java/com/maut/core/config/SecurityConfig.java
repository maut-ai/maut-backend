package com.maut.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Permit unauthenticated POST requests to the client applications endpoint
                .mvcMatchers(HttpMethod.POST, "/v1/admin/client-applications").permitAll()
                // Permit unauthenticated GET requests to the status endpoint for health checks
                .mvcMatchers(HttpMethod.GET, "/v1/status").permitAll()
                // Permit unauthenticated GET requests to the hello endpoint
                .mvcMatchers(HttpMethod.GET, "/v1/hello").permitAll()
                // Permit unauthenticated POST requests to the session endpoint
                .mvcMatchers(HttpMethod.POST, "/v1/session").permitAll()
                // All other requests should be authenticated
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
