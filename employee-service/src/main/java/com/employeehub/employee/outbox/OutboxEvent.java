package com.employeehub.employee.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Transactional Outbox table. Business changes and the event that describes
 * them are written in the SAME database transaction. Debezium then tails the
 * PostgreSQL WAL and publishes each row to Kafka (topic employeehub.EMPLOYEE).
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;
}

