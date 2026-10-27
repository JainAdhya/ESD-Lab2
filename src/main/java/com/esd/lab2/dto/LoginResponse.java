package com.esd.lab2.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public LoginResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
