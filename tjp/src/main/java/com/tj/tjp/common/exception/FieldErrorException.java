package com.tj.tjp.common.exception;

import lombok.Getter;

@Getter
public class FieldErrorException extends RuntimeException {
    private final String field;

    public FieldErrorException(String message, String field) {
        super(message);
        this.field = field;
    }
}

