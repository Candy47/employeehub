package com.employeehub.notification.consumer;

import com.employeehub.notification.event.UserRegisteredEvent;
import com.employeehub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "employeehub.USER",
            groupId = "notification-service-group"
    )
    public void consume(
            UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║              USER REGISTERED EVENT RECEIVED                 ║
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

        notificationService.sendWelcomeNotification(event);
    }
}