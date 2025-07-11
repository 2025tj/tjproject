package com.tj.tjp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class OptionalPasswordValidator implements ConstraintValidator<OptionalPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            // null 또는 빈 문자열이면 검사 통과
            return true;
        }
        // 비밀번호가 입력되었을 경우 최소 길이가 8자 이상만 허용
        return value.length() >=8;
    }
}
