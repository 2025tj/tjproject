package com.tj.tjp.service;

import com.tj.tjp.dto.SignupRequest;
import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Long signup(SignupRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user= User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Set.of("ROLE_USER"))
                .build();

        return userRepository.save(user).getId();
    }

}
