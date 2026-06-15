package com.gabriel.fintransfer.wallet.exception;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class WalletNotFoundException extends BusinessException {

    public WalletNotFoundException(String detail) {
        super("Wallet not found: " + detail, HttpStatus.NOT_FOUND);
    }
}
