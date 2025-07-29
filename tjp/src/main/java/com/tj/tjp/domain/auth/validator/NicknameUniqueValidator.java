package com.tj.tjp.domain.auth.validator;

import com.tj.tjp.domain.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NicknameUniqueValidator implements ConstraintValidator<NicknameUnique, String> {

    private final UserRepository userRepository;


    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        if (nickname == null || nickname.isBlank()) return true; // NotBlank는 별도로 처리
        return !userRepository.findByNickname(nickname).isPresent();
    }
}
