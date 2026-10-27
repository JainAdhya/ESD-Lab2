package com.esd.lab2.controller;

import java.time.Instant;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esd.lab2.dto.LoginRequest;
import com.esd.lab2.dto.LoginResponse;
import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.security.JwtService;
import com.esd.lab2.security.TokenDenylist;
import com.esd.lab2.service.AuthService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

/**
 * S1 — FILL IN MANUALLY (no AI for this slice).
 *
 * This controller handles all /api/auth endpoints.
 * In S1 you add the signup endpoint.
 * In S2 (AI) you will add login and later logout.
 *
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenDenylist tokenDenylist;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            TokenDenylist tokenDenylist) {

        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenDenylist = tokenDenylist;
    }

    /**
     * A1 — POST /api/auth/signup
     * Public endpoint. Returns 201 with UserResponse on success.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        // TODO: call authService.signup(request) and return 201 with the result
        UserResponse response = authService.signup(request);

    return ResponseEntity.status(201).body(response);
    }

    // S2: login endpoint will be added here by AI
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
    // S5: logout endpoint will be added here by AI
    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims = jwtService.parseToken(token);

            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (jti == null || jti.isBlank() || expiration == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Instant expiry = expiration.toInstant();

            tokenDenylist.revoke(jti, expiry);

            return ResponseEntity.noContent().build();

        } catch (io.jsonwebtoken.JwtException
                | IllegalArgumentException ex) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
