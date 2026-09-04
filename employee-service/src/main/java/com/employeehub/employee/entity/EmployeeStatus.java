package com.employeehub.employee.entity;

/**
 * Lifecycle of an employee profile.
 *
 * PENDING_ONBOARDING - profile auto-created from a UserRegistered event,
 *                      HR details not filled yet.
 * ACTIVE             - profile completed and the employee is active.
 * INACTIVE           - employee has left / been deactivated.
 */
public enum EmployeeStatus {
    PENDING_ONBOARDING,
    ACTIVE,
    INACTIVE
}
