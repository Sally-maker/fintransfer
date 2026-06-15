package com.gabriel.fintransfer.notification.dto;

import java.math.BigDecimal;

public record NotificationRequest(String recipientEmail, BigDecimal amount, String senderName) {}
