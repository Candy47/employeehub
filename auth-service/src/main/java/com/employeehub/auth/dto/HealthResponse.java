package com.employeehub.auth.dto;

public class HealthResponse {

    private String status;

    public HealthResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}