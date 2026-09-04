package com.employeehub.employee.service;

import com.employeehub.employee.dto.EmployeeResponse;
import com.employeehub.employee.dto.UpdateEmployeeRequest;
import com.employeehub.employee.events.UserRegisteredEvent;

import java.util.List;

public interface EmployeeService {

    /**
     * Reacts to a UserRegistered event by auto-provisioning a draft employee
     * profile (status PENDING_ONBOARDING) and emitting an EmployeeCreated
     * event through the outbox.
     */
    void provisionFromUserRegistered(UserRegisteredEvent event);

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Long id);

    EmployeeResponse findByEmail(String email);

    /**
     * HR completes / updates a profile. Emits an EmployeeUpdated event through
     * the outbox in the same transaction.
     */
    EmployeeResponse update(Long id, UpdateEmployeeRequest request);
}

