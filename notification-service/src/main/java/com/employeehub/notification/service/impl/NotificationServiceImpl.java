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

        log.info(
                "Sending welcome notification to userId={}, email={}, name={}",
                event.getUserId(),
                event.getEmail(),
                event.getFullName()
        );
    }
}