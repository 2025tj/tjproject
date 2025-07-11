package com.tj.tjp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalNicknameValidator implements ConstraintValidator<OptionalNickname, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; //입력이 없으면 검사안함
        }
        int length = value.trim().length();
        return length>=2 && length <=20;
    }
}
