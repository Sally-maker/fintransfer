package com.gabriel.fintransfer.wallet.exception;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException() {
        super("Insufficient balance for the requested transfer", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
