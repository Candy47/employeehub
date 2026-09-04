package com.employeehub.employee.service.impl;

import com.employeehub.employee.dto.EmployeeResponse;
import com.employeehub.employee.dto.UpdateEmployeeRequest;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.entity.EmployeeStatus;
import com.employeehub.employee.events.EmployeeEvent;
import com.employeehub.employee.events.UserRegisteredEvent;
import com.employeehub.employee.outbox.OutboxEvent;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.employee.repository.OutboxEventRepository;
import com.employeehub.employee.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    /** Aggregate type routed by Debezium -> topic employeehub.EMPLOYEE. */
    private static final String AGGREGATE_TYPE = "EMPLOYEE";

    private final EmployeeRepository employeeRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void provisionFromUserRegistered(UserRegisteredEvent event) {

        // Idempotency: a Kafka message can be redelivered. Never create twice.
        if (employeeRepository.existsByUserId(event.getUserId())) {
            log.info("Employee already exists for userId={}, skipping provisioning",
                    event.getUserId());
            return;
        }

        Instant now = Instant.now();

        Employee employee = Employee.builder()
                .userId(event.getUserId())
                .fullName(event.getFullName())
                .email(event.getEmail())
                .status(EmployeeStatus.PENDING_ONBOARDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Employee saved = employeeRepository.save(employee);

        writeOutbox(saved, "EmployeeCreated", now);

        log.info("Provisioned employee id={} for userId={} (PENDING_ONBOARDING)",
                saved.getId(), saved.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setManagerId(request.getManagerId());

        // Default a completed profile to ACTIVE unless caller overrides.
        employee.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : EmployeeStatus.ACTIVE
        );

        Instant now = Instant.now();
        employee.setUpdatedAt(now);

        Employee saved = employeeRepository.save(employee);

        writeOutbox(saved, "EmployeeUpdated", now);

        log.info("Updated employee id={} (status={})", saved.getId(), saved.getStatus());

        return toResponse(saved);
    }

    /**
     * Writes the business event to the outbox table in the SAME transaction as
     * the employee change. Debezium publishes it to Kafka asynchronously.
     */
    private void writeOutbox(Employee employee, String eventType, Instant occurredAt) {

        UUID eventId = UUID.randomUUID();

        EmployeeEvent event = EmployeeEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .employeeId(employee.getId())
                .userId(employee.getUserId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .status(employee.getStatus().name())
                .occurredAt(occurredAt)
                .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(employee.getId().toString())
                .eventType(eventType)
                .payload(convertToJson(event))
                .createdAt(occurredAt)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUserId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .managerId(employee.getManagerId())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    private String convertToJson(EmployeeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize EmployeeEvent",
                    exception
            );
        }
    }
}

