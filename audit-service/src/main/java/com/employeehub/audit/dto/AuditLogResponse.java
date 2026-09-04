package com.employeehub.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditLogResponse {

    private Long id;
    private String subject;
    private String eventType;
    private String sourceTopic;
    private String details;
    private Instant recordedAt;
}

