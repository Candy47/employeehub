package com.employeehub.employee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user in auth-service. We store a copy of the identity
     * data (data duplication via events) instead of querying auth's database.
     */
    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String department;

    @Column
    private String designation;

    @Column
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
