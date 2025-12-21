package com.urbansphere.authservice.service;

import com.urbansphere.authservice.dto.AuthResponse;
import com.urbansphere.authservice.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {
    AuthResponse registerUser(@Valid RegisterRequest requestDto);
}
