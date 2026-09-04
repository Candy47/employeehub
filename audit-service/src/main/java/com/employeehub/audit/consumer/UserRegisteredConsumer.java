package com.employeehub.audit.consumer;

import com.employeehub.audit.event.UserRegisteredEvent;
import com.employeehub.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes UserRegistered events (employeehub.USER) into the audit log.
 * This is one of two independent consumer groups on this topic (the other is
 * employee-service) - demonstrating event fan-out.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final AuditService auditService;

    @KafkaListener(
            topics = "employeehub.USER",
            groupId = "audit-service-group"
    )
    public void consume(
            UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║              USER REGISTERED EVENT RECEIVED (audit)        ║
                ╠══════════════════════════════════════════════════════════════╣
                ║ Event ID : {}
                ║ User ID  : {}
                ║ Name     : {}
                ║ Email    : {}
                ║ Topic    : employeehub.USER
                ║ Key      : {}
                ╚══════════════════════════════════════════════════════════════╝
                """,
                event.getEventId(),
                event.getUserId(),
                event.getFullName(),
                event.getEmail(),
                key
        );

        auditService.recordUserRegistered(event);
    }
}

