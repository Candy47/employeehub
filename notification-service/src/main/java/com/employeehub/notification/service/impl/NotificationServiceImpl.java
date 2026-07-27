package com.employeehub.notification.service.impl;

import com.employeehub.notification.event.UserRegisteredEvent;
import com.employeehub.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendWelcomeNotification(UserRegisteredEvent event) {

        log.info("""
            
            ────────────────────────────────────────────────────────────────
                     PROCESSING USER REGISTRATION EVENT
            ────────────────────────────────────────────────────────────────
            Event ID : {}
            Email    : {}
            """,
                event.getEventId(),
                event.getEmail()
        );

        if (event.getEmail().contains("fail")) {
            throw new RuntimeException("Simulated notification failure");
        }

        log.info("""
            
            ✅ EVENT PROCESSED SUCCESSFULLY
            
            User ID : {}
            Name    : {}
            Email   : {}
            """,
                event.getUserId(),
                event.getFullName(),
                event.getEmail()
        );
    }
}