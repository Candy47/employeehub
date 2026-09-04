package com.employeehub.employee.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Outgoing event published (via the Outbox + Debezium) to topic
 * employeehub.EMPLOYEE. A single payload shape is reused for both
 * "EmployeeCreated" and "EmployeeUpdated"; the {@code eventType} field
 * distinguishes them so downstream consumers can react accordingly.
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
