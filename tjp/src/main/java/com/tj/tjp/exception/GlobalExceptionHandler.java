package com.tj.tjp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

    @ExceptionHandler(OAuth2LinkRequiredException.class)
    public ResponseEntity<?> handleLinkRequired(OAuth2LinkRequiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "SOCIAL_LINK_REQUIRED",
                "email", ex.getEmail(),
                "provider", ex.getProvider()
        ));
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<?> handleOAuth2Error(OAuth2AuthenticationException ex) {
        OAuth2Error error = ex.getError();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", error.getErrorCode(),
                "message", error.getDescription()
        ));
    }
}
