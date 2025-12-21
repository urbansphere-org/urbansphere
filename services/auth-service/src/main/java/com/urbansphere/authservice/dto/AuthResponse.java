package com.urbansphere.authservice.dto;

public record AuthResponse(String accessToken, String refreshToken) {
}
