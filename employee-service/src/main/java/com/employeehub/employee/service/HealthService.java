package com.employeehub.employee.service;

import com.employeehub.employee.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("UP");
    }
}

