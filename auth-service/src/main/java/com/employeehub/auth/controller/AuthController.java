package com.employeehub.auth.controller;

import com.employeehub.auth.dto.*;
import com.employeehub.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request) {

        System.out.println("LOGIN Controller");
        AuthResult authResult = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("jwt", authResult.getToken())
                .httpOnly(true)
                .secure(false) // true in production
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(1))
                .build();

        LoginResponse response = LoginResponse.builder()
                .id(authResult.getUser().getId())
                .fullName(authResult.getUser().getFullName())
                .email(authResult.getUser().getEmail())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return authentication.getName();
    }
}