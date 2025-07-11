package com.tj.tjp.service;

import com.tj.tjp.dto.LocalSignupRequest;
import com.tj.tjp.dto.SignupRequest;
import com.tj.tjp.dto.UpdateUserRequest;
import com.tj.tjp.entity.ProviderType;
import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
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

    @Transactional
    public Long localSignup(LocalSignupRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (userRepository.findByNickname(dto.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .nickname(dto.nickname())
                .provider(dto.provider()) // providerType.LOCAL
                .roles(Set.of("ROLE_USER"))
                .build();
        Long id = userRepository.save(user).getId();
        log.info("회원가입 성공: {} (id : {})", user.getEmail(), id);
        return  id;
    }

    public void updateUserInfo(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            user.updateProfile(request.getNickname());
        }
        userRepository.save(user);
    }

    @Transactional
    public void setPassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            throw new IllegalStateException("이미 비밀번호가 설정되어 있습니다.");
        }
        user.updatePassword(passwordEncoder.encode(rawPassword));
    }

    public void linkSoicalAccount(String email, ProviderType provider) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getProvider() != ProviderType.LOCAL) {
            throw new IllegalStateException("이미 소셜 연동된 계정입니다.");
        }

        user.updateProvider(provider);
        userRepository.save(user);
    }


}
