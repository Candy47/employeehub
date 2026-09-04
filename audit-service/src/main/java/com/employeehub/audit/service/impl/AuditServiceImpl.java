package com.employeehub.audit.service.impl;

import com.employeehub.audit.entity.AuditLog;
import com.employeehub.audit.event.EmployeeEvent;
import com.employeehub.audit.event.UserRegisteredEvent;
import com.employeehub.audit.repository.AuditLogRepository;
import com.employeehub.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void recordUserRegistered(UserRegisteredEvent event) {

        log.info("""
            
            ────────────────────────────────────────────────────────────────
                     RECORDING USER REGISTRATION EVENT
            ────────────────────────────────────────────────────────────────
            Event ID : {}
            Subject  : {}
            """,
                event.getEventId(),
                event.getEmail()
        );

        if (event.getEmail().contains("fail")) {
            throw new RuntimeException("Simulated audit failure");
        }

        String details = "User " + event.getFullName()
                + " (id " + event.getUserId() + ") registered.";

        record(event.getEmail(), "UserRegistered", "employeehub.USER", details);

        log.info("✅ AUDIT RECORDED: UserRegistered for {}", event.getEmail());
    }

    @Override
    public void recordEmployeeEvent(EmployeeEvent event) {

        log.info("""
            
            ────────────────────────────────────────────────────────────────
                     RECORDING EMPLOYEE EVENT
            ────────────────────────────────────────────────────────────────
            Event ID : {}
            Type     : {}
            Subject  : {}
            """,
                event.getEventId(),
                event.getEventType(),
                event.getEmail()
        );

        if (event.getEmail() != null && event.getEmail().contains("fail")) {
            throw new RuntimeException("Simulated audit failure");
        }

        String details = switch (event.getEventType() == null ? "" : event.getEventType()) {
            case "EmployeeCreated" ->
                    "Employee profile created - onboarding pending";
            case "EmployeeUpdated" ->
                    "Employee profile updated (status: " + event.getStatus() + ")";
            default -> "Employee event processed";
        };

        record(
                event.getEmail(),
                event.getEventType() != null ? event.getEventType() : "EmployeeEvent",
                "employeehub.EMPLOYEE",
                details
        );

        log.info("✅ AUDIT RECORDED: {} for {}", event.getEventType(), event.getEmail());
    }

    private void record(String subject, String eventType, String sourceTopic, String details) {
        auditLogRepository.save(
                AuditLog.builder()
                        .subject(subject)
                        .eventType(eventType)
                        .sourceTopic(sourceTopic)
                        .details(details)
                        .recordedAt(Instant.now())
                        .build()
        );
    }
}

