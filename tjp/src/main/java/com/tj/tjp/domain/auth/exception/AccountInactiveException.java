package com.tj.tjp.domain.auth.exception;

public class AccountInactiveException extends RuntimeException{
    public AccountInactiveException(String message) {
        super(message);
    }
}
