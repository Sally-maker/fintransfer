package com.gabriel.fintransfer.transaction.dto;

import com.gabriel.fintransfer.transaction.domain.Transaction;
import com.gabriel.fintransfer.transaction.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        TransactionStatus status,
        String authorizationReason,
        LocalDateTime createdAt
) {

    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getPayer().getId(),
                tx.getPayee().getId(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getAuthorizationReason(),
                tx.getCreatedAt()
        );
    }
}
