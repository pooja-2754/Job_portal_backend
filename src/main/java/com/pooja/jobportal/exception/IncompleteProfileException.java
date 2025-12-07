package com.pooja.jobportal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Profile incomplete")
public class IncompleteProfileException extends RuntimeException {
    public IncompleteProfileException(String message) {
        super(message);
    }
}