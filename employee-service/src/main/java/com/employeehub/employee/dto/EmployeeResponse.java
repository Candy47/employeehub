package com.employeehub.employee.dto;

import com.employeehub.employee.entity.EmployeeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EmployeeResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String designation;
    private Long managerId;
    private EmployeeStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

