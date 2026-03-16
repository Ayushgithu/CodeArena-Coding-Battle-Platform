package com.codearena.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}