package com.employeehub.employee.dto;

import com.employeehub.employee.entity.EmployeeStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload HR uses to complete / update an employee profile.
 * Identity fields (name, email, userId) are NOT editable here - they are
 * owned by auth-service and arrive via the UserRegistered event.
 */
@Data
public class UpdateEmployeeRequest {

    @NotBlank
    private String department;

    @NotBlank
    private String designation;

    private Long managerId;

    private EmployeeStatus status;
}

