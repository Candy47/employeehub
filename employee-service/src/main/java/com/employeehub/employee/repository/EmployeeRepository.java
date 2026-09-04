package com.employeehub.employee.repository;

import com.employeehub.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmail(String email);

    boolean existsByUserId(Long userId);
}
