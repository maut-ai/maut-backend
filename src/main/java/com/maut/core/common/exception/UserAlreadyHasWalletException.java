package com.maut.core.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // As per API spec: 403 Forbidden
public class UserAlreadyHasWalletException extends RuntimeException {

    public UserAlreadyHasWalletException(String message) {
        super(message);
    }

    public UserAlreadyHasWalletException(String message, Throwable cause) {
        super(message, cause);
    }
}
