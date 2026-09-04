package com.employeehub.audit.consumer;

import com.employeehub.audit.event.EmployeeEvent;
import com.employeehub.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes employee lifecycle events (employeehub.EMPLOYEE) into the audit log.
 * Uses a dedicated container factory so the payload is deserialized as
 * {@link EmployeeEvent}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventConsumer {

    private final AuditService auditService;

    @KafkaListener(
            topics = "employeehub.EMPLOYEE",
            groupId = "audit-service-group",
            containerFactory = "employeeKafkaListenerContainerFactory"
    )
    public void consume(
            EmployeeEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║                EMPLOYEE EVENT RECEIVED (audit)            ║
                ╠══════════════════════════════════════════════════════════════╣
                ║ Event ID : {}
                ║ Type     : {}
                ║ Emp ID   : {}
                ║ Name     : {}
                ║ Dept     : {}
                ║ Status   : {}
                ║ Topic    : employeehub.EMPLOYEE
                ║ Key      : {}
                ╚══════════════════════════════════════════════════════════════╝
                """,
                event.getEventId(),
                event.getEventType(),
                event.getEmployeeId(),
                event.getFullName(),
                event.getDepartment(),
                event.getStatus(),
                key
        );

        auditService.recordEmployeeEvent(event);
    }
}

