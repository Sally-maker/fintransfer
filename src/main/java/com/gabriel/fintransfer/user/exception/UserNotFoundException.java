package com.gabriel.fintransfer.user.exception;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String id) {
        super("User not found: " + id, HttpStatus.NOT_FOUND);
    }
}
