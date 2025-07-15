package com.tj.tjp.service.user;

import com.tj.tjp.dto.auth.singup.SignupRequest;
import com.tj.tjp.dto.user.UpdateUserRequest;
import com.tj.tjp.dto.user.UserUpdateResult;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.event.UserSignupEvent;
import com.tj.tjp.exception.EmailNotVerifiedException;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.service.email.EmailVerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long signup(SignupRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (userRepository.findByNickname(dto.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        User user= User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .nickname(dto.nickname())
                .roles(Set.of("ROLE_USER"))
                .build();
        Long id = userRepository.save(user).getId();

        // *** 이메일 인증 메일 발송 *** (회원가입 성공시점에 이밴트 발행, 트랜잭션 커밋 후 실행됨)
        eventPublisher.publishEvent(new UserSignupEvent(user));

        return id;
    }

    public UserUpdateResult updateUserInfo(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String warning = null;
        if (!user.isEmailVerified()) {
            LocalDateTime expireLimit = user.getCreatedAt().plusDays(7);
            if (LocalDateTime.now().isAfter(expireLimit)) {
                throw new EmailNotVerifiedException("이메일 인증 유예기간이 만료되었습니다. 로그인 불가. 인증메일 재전송 필요");
            } else {
                warning = "이메일 인증이 필요합니다. 7일 이내 인증하지 않으면 계정이 비활성화됩니다.";
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            user.updateProfile(request.getNickname());
        }
        userRepository.save(user);

        return new UserUpdateResult(true, warning);
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

//    public void linkSoicalAccount(String email, ProviderType provider) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        if (user.getProvider() != ProviderType.LOCAL) {
//            throw new IllegalStateException("이미 소셜 연동된 계정입니다.");
//        }
//
//        user.updateProvider(provider);
//        userRepository.save(user);
//    }

//    public User linkSocialAccount(String email, ProviderType provider) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
//
//        // 이미 연동된 경우 예외 처리
//        if (user.getProvider() != null) {
//            throw new IllegalArgumentException("이미 연동된 계정입니다.");
//        }
//        user.updateProvider(provider); // 혹은 연동 테이블을 두어 관계 맺기?
//        return userRepository.save(user);
//    }

}
