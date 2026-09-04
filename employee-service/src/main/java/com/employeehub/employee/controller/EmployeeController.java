package com.employeehub.employee.controller;

import com.employeehub.employee.dto.EmployeeResponse;
import com.employeehub.employee.dto.UpdateEmployeeRequest;
import com.employeehub.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    /** Returns the profile of the currently authenticated user (from the JWT). */
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> me(Authentication authentication) {
        return ResponseEntity.ok(
                employeeService.findByEmail(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {

        return ResponseEntity.ok(employeeService.update(id, request));
    }
}

