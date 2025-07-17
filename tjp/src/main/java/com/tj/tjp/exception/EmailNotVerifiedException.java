package com.tj.tjp.exception;

import lombok.Getter;

@Getter
public class EmailNotVerifiedException extends RuntimeException{
    private final String email;
    private final boolean canResend;
    public EmailNotVerifiedException(String message) {
        this(message, null, true);
    }
    public EmailNotVerifiedException(String message, String email) {
        this(message, email, true);
    }
    public EmailNotVerifiedException(String message, String email, boolean canResend) {
        super(message);
        this.email = email;
        this.canResend =canResend;
    }
}
