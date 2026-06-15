package com.gabriel.fintransfer.transaction.exception;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UnauthorizedTransactionException extends BusinessException {

    public UnauthorizedTransactionException(String reason) {
        super("Transaction not authorized: " + reason, HttpStatus.FORBIDDEN);
    }
}
