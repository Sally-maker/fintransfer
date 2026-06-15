package com.gabriel.fintransfer.notification.service;

import com.gabriel.fintransfer.notification.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogNotificationService implements NotificationService {

    @Override
    public void notify(NotificationRequest request) {
        log.info("NOTIFICATION → To: {} | {} sent you R$ {}",
                request.recipientEmail(), request.senderName(), request.amount());
    }
}
