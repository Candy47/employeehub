package com.employeehub.employee.repository;

import com.employeehub.employee.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {
}

