package com.employeehub.audit.service;

import com.employeehub.audit.event.EmployeeEvent;
import com.employeehub.audit.event.UserRegisteredEvent;

public interface AuditService {

    void recordUserRegistered(UserRegisteredEvent event);

    void recordEmployeeEvent(EmployeeEvent event);
}

