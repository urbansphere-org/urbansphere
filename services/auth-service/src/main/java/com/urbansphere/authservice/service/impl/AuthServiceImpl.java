package com.urbansphere.authservice.service.impl;

import com.urbansphere.authservice.dto.AuthResponse;
import com.urbansphere.authservice.dto.RegisterRequest;
import com.urbansphere.authservice.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse registerUser(RegisterRequest requestDto) {
        return  null;
    }
}
