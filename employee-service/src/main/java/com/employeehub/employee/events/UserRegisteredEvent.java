package com.employeehub.employee.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Incoming event consumed from topic employeehub.USER (produced by auth-service).
 * Mirrors the auth-service payload shape.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private UUID eventId;
    private Long userId;
    private String fullName;
    private String email;
    private Instant occurredAt;
}

