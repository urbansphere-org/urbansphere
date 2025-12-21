package com.urbansphere.authservice.controller;


import com.urbansphere.authservice.common.ApiResponse;
import com.urbansphere.authservice.dto.AuthResponse;
import com.urbansphere.authservice.dto.RegisterRequest;
import com.urbansphere.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @Valid
            @RequestBody
            RegisterRequest request
    ){
        AuthResponse authResponse = authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", authResponse));
    }

}
