package com.gabriel.fintransfer.transaction.authorization;

import com.gabriel.fintransfer.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public record RefundContext(
        String requesterName,
        String recipientName,
        BigDecimal amount,
        String reason,
        long minutesSinceTransaction
) {

    public static RefundContext of(Transaction tx, String requesterName, String recipientName, String reason) {
        long minutes = Duration.between(tx.getCreatedAt(), LocalDateTime.now()).toMinutes();
        return new RefundContext(requesterName, recipientName, tx.getAmount(), reason, minutes);
    }
}
