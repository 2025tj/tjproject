package com.tj.tjp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseBody
    public Map<String, String> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return Map.of(
                "status", "UNVERIFIED_EMAIL",
                "message", ex.getMessage()
        );
    }
}
