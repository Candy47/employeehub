package com.employeehub.employee.dto;

public class HealthResponse {

    private String status;

    public HealthResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

