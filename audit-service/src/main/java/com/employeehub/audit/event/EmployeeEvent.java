package com.employeehub.audit.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event consumed from topic employeehub.EMPLOYEE (produced by employee-service).
 * A single shape covers both "EmployeeCreated" and "EmployeeUpdated"; the
 * {@code eventType} field distinguishes them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEvent {

    private UUID eventId;
    private String eventType;
    private Long employeeId;
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String designation;
    private String status;
    private Instant occurredAt;
}

