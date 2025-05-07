package com.maut.core.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // As per API spec: 500 Internal Server Error
public class TurnkeyOperationException extends RuntimeException {

    public TurnkeyOperationException(String message) {
        super(message);
    }

    public TurnkeyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
