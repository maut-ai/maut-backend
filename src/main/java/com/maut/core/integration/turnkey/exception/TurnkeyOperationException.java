package com.maut.core.integration.turnkey.exception;

public class TurnkeyOperationException extends RuntimeException {

    public TurnkeyOperationException(String message) {
        super(message);
    }

    public TurnkeyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
