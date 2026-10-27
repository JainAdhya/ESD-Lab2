package com.esd.lab2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.esd.lab2.security.JsonAccessDeniedHandler;
import com.esd.lab2.security.JsonAuthenticationEntryPoint;
import com.esd.lab2.security.JwtAuthenticationFilter;
/**
 * TEMPORARY SecurityConfig — permits all requests so Slices 1 and 2 work
 * without authentication in place.
 *
 * NOTE: Replace the filterChain bean in Slice 3.
 *       Do NOT touch this file before Slice 3.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            JsonAccessDeniedHandler jsonAccessDeniedHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                .accessDeniedHandler(jsonAccessDeniedHandler))

            .authorizeHttpRequests(auth -> auth

    .requestMatchers(
        HttpMethod.POST,
        "/api/auth/signup",
        "/api/auth/login"
    ).permitAll()

    // Individual course lookup remains public.
    .requestMatchers(
        HttpMethod.GET,
        "/api/courses/*"
    ).permitAll()

    // Creating courses requires ADMIN.
    .requestMatchers(
        HttpMethod.POST,
        "/api/courses"
    ).hasRole("ADMIN")

    // All remaining endpoints require authentication.
    .anyRequest().authenticated()
)

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}