package com.tj.tjp.domain.auth.security.exception;

public class RefreshTokenReuseDetectedException extends RuntimeException{
    public RefreshTokenReuseDetectedException() {
        super("Refresh token reuse detected");
    }
}
