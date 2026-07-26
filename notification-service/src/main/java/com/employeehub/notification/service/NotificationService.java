package com.employeehub.notification.service;

import com.employeehub.notification.event.UserRegisteredEvent;

public interface NotificationService {

    void sendWelcomeNotification(UserRegisteredEvent event);
}