package com.employeehub.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * An immutable audit record of a domain event this service observed on Kafka.
 * This is the queryable event log / activity history for EmployeeHub.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who the event was about (email). */
    @Column(nullable = false)
    private String subject;

    /** The source event type, e.g. UserRegistered / EmployeeCreated. */
    @Column(nullable = false)
    private String eventType;

    /** The Kafka topic the event was consumed from. */
    @Column(nullable = false)
    private String sourceTopic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant recordedAt;
}

