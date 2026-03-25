package com.rwa.common.exception;

public class RwaBaseException extends RuntimeException {
    public RwaBaseException(String message) {
        super(message);
    }
    public RwaBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
