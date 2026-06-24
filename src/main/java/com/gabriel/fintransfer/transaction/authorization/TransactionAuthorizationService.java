package com.gabriel.fintransfer.transaction.authorization;

public interface TransactionAuthorizationService {

    AuthorizationResult authorize(TransactionContext context);

    AuthorizationResult authorizeRefund(RefundContext context);
}
