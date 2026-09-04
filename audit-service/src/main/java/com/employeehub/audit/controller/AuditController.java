package com.employeehub.audit.controller;

import com.employeehub.audit.dto.AuditLogResponse;
import com.employeehub.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes the audit / event log. Reached through the API Gateway, which
 * validates the JWT before forwarding the request.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLogResponse> history() {
        return auditLogRepository.findAllByOrderByRecordedAtDesc()
                .stream()
                .map(a -> AuditLogResponse.builder()
                        .id(a.getId())
                        .subject(a.getSubject())
                        .eventType(a.getEventType())
                        .sourceTopic(a.getSourceTopic())
                        .details(a.getDetails())
                        .recordedAt(a.getRecordedAt())
                        .build())
                .toList();
    }
}

