package com.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/manage/health/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Protected endpoints - require authentication
                        .requestMatchers("/api/v1/notifications/**").authenticated()
                        .requestMatchers("/api/v1/templates/**").authenticated()
                        .requestMatchers("/api/v1/analytics/**").authenticated()
                        .requestMatchers("/api/v1/bulk/**").authenticated()
                        .requestMatchers("/api/v1/retry/**").authenticated()

                        .anyRequest().authenticated()
                );

        // For H2 console in development
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
        );

        return http.build();
    }
}
