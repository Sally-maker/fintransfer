package com.gabriel.fintransfer.transaction.authorization;

import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;

import java.math.BigDecimal;

public record TransactionContext(
        String payerName,
        UserType payerType,
        String payeeName,
        UserType payeeType,
        BigDecimal payerBalance,
        BigDecimal amount
) {

    public static TransactionContext of(User payer, User payee, BigDecimal payerBalance, BigDecimal amount) {
        return new TransactionContext(
                payer.getName(), payer.getUserType(),
                payee.getName(), payee.getUserType(),
                payerBalance, amount
        );
    }
}
