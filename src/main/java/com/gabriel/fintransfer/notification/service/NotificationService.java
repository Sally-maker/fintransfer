package com.gabriel.fintransfer.notification.service;

import com.gabriel.fintransfer.notification.dto.NotificationRequest;

public interface NotificationService {

    void notify(NotificationRequest request);
}
