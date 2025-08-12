package com.tj.tjp.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountStatusException extends AuthenticationException {
    public AccountStatusException(String msg) {
        super(msg);
    }
}
