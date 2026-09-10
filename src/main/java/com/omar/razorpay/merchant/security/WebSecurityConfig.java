package com.omar.razorpay.merchant.security;

import com.omar.razorpay.merchant.security.filters.ApiKeyAuthenticationFilter;
import com.omar.razorpay.merchant.security.filters.IdempotencyFilter;
import com.omar.razorpay.merchant.security.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final String[] JWT_ROUTES = {"/v1/auth/**", "/v1/merchants/**", "/v1/admin/**", "/actuator/**"};
    private static final String[] API_KEY_ROUTES = {"/v1/orders/**", "/v1/payments/**", "/v1/vault/**"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final IdempotencyFilter idempotencyFilter;

    @Bean
    public SecurityFilterChain jwtChain(HttpSecurity http) {
        return http
                .securityMatcher(JWT_ROUTES)
                .csrf(csrf ->
                        csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/v1/auth/signup", "/v1/auth/login").permitAll()
                                .anyRequest().authenticated())
                .formLogin(form ->
                        form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(idempotencyFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Bean
    public SecurityFilterChain apiKeyChain(HttpSecurity http) {
        return http
                .securityMatcher(API_KEY_ROUTES)
                .csrf(csrf ->
                        csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated())
                .formLogin(form ->
                        form.disable())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(idempotencyFilter, ApiKeyAuthenticationFilter.class)
                .build();
    }
}