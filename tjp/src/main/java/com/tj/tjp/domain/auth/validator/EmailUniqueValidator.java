package com.tj.tjp.domain.auth.validator;

import com.tj.tjp.domain.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailUniqueValidator implements ConstraintValidator<EmailUnique, String> {
    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) return true; // NotBlank는 따로 처리
        return !userRepository.findByEmail(email).isPresent();
    }
}
