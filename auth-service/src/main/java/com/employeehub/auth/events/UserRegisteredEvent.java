package com.employeehub.auth.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

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