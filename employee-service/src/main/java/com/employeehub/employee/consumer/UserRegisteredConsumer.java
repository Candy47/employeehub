package com.employeehub.employee.consumer;

import com.employeehub.employee.events.UserRegisteredEvent;
import com.employeehub.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Listens to the SAME topic as audit-service (employeehub.USER) but in a
 * different consumer group, so both services receive every UserRegistered event
 * independently (event fan-out / choreography).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final EmployeeService employeeService;

    @KafkaListener(
            topics = "employeehub.USER",
            groupId = "employee-service-group"
    )
    public void consume(
            UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║          USER REGISTERED EVENT RECEIVED (employee)         ║
                ╠══════════════════════════════════════════════════════════════╣
                ║ Event ID : {}
                ║ User ID  : {}
                ║ Name     : {}
                ║ Email    : {}
                ║ Topic    : employeehub.USER
                ║ Key      : {}
                ╚══════════════════════════════════════════════════════════════╝
                """,
                event.getEventId(),
                event.getUserId(),
                event.getFullName(),
                event.getEmail(),
                key
        );

        employeeService.provisionFromUserRegistered(event);
    }
}


