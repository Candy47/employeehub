package com.employeehub.auth.service.impl;

import com.employeehub.auth.dto.AuthResult;
import com.employeehub.auth.dto.LoginRequest;
import com.employeehub.auth.dto.RegisterRequest;
import com.employeehub.auth.dto.RegisterResponse;
import com.employeehub.auth.entity.Role;
import com.employeehub.auth.entity.User;
import com.employeehub.auth.events.UserRegisteredEvent;
import com.employeehub.auth.outbox.OutboxEvent;
import com.employeehub.auth.repository.OutboxEventRepository;
import com.employeehub.auth.repository.UserRepository;
import com.employeehub.auth.security.JwtService;
import com.employeehub.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = repository.save(user);

        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(eventId)
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .occurredAt(occurredAt)
                .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("USER")
                .aggregateId(savedUser.getId().toString())
                .eventType("UserRegistered")
                .payload(convertToJson(event))
                .createdAt(occurredAt)
                .build();

        outboxEventRepository.save(outboxEvent);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .build();
    }

    @Override
    public AuthResult login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return AuthResult.builder()
                .user(user)
                .token(token)
                .build();
    }

    private String convertToJson(UserRegisteredEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize UserRegisteredEvent",
                    exception
            );
        }
    }
}