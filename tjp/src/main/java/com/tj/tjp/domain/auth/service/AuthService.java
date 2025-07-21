// AuthService.java
package com.tj.tjp.domain.auth.service;

import com.tj.tjp.domain.auth.dto.login.LoginResult;
import com.tj.tjp.event.EmailVerificationResendEvent;
import com.tj.tjp.common.exception.EmailNotVerifiedException;
import com.tj.tjp.domain.user.repository.UserRepository;
import com.tj.tjp.domain.auth.security.principal.AuthenticatedUser;
import com.tj.tjp.domain.auth.security.service.TokenService;
import com.tj.tjp.domain.social.service.SocialAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountService socialAccountService;


    @Value("${app.auth.verification-grace-minutes}")
    private long gracePeriodMinutes;

    /**
     * 일반 로그인(email/password) 처리
     */
    @Transactional
    public LoginResult login(String email, String password, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // 이메일 인증 유예기간 체크 및 상태 변경, 메일 재발송
        if (!user.getUser().isEmailVerified()) {
//            LocalDateTime expireLimit = user.getUser().getCreatedAt().plusDays(7);
            LocalDateTime expireLimit = user.getUser().getCreatedAt().plusMinutes(gracePeriodMinutes);
            if (LocalDateTime.now().isAfter(expireLimit)) {
                // 상태 변경
                user.getUser().deactivate();
                userRepository.save(user.getUser());

                // 인증 메일 재발송 이벤트 발행
                eventPublisher.publishEvent(new EmailVerificationResendEvent(user.getUser()));

                // 예외 던져 로그인 차단
                throw new EmailNotVerifiedException("이메일 인증 유예기간이 만료되었습니다. 로그인 불가. 인증메일 재전송 완료.");
            }
        }

        // 1) 액세스 토큰은 헤더에만
        tokenService.issueAccessTokenHeader(response, user.getEmail(), roles);

        // 2) 리프레시 토큰은 HttpOnly 쿠키에만
        tokenService.issueRefreshTokenCookie(response, user.getEmail());

        // 2) 경고 메시지 판단
        String warning = null;
        if (!user.getUser().isEmailVerified()) {
            warning = "이메일 인증이 필요합니다. 7일 이내 인증하지 않으면 계정이 비활성화됩니다.";
        }
        return new LoginResult(user.getEmail(), "로그인 성공", warning);
    }

//    /**
//     * 소셜 회원가입 처리:
//     * 1) 이메일/닉네임 중복 체크
//     * 2) User 생성 (비밀번호는 받아서 해시, 이메일은 이미 검증된 것으로 처리)
//     * 3) emailVerified=true, emailVerifiedAt 설정
//     * 4) SocialAccount 연결
//     * 5) 가입 이벤트 발행
//     */
//    @Transactional
//    public Long signupOAuth2(OAuth2SignupRequest dto) {
//        // 1) 중복 검사
//        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
//            throw new DuplicateUserException("이미 가입된 이메일입니다.");
//        }
//        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
//            throw new DuplicateUserException("이미 사용중인 닉네임입니다.");
//        }
//
//        // 2) User 엔티티 생성
//        User user = User.builder()
//                .email(dto.getEmail())
//                // 소셜가입이라 해도 비밀번호를 직접 설정하도록 요구
//                .password(passwordEncoder.encode(dto.getPassword()))
//                .nickname(dto.getNickname())
//                .roles(Set.of("ROLE_USER"))
//                // emailVerified를 true로 설정 (소셜 쪽에서 이미 인증했으므로)
//                .emailVerified(true)
//                .emailVerifiedAt(LocalDateTime.now())
//                .build();
//        userRepository.save(user);
//
//        // 3) SocialAccount 연결
//        socialAccountService.linkSocialAccount(
//                user,
//                dto.getProvider(),
//                dto.getProviderId()
//        );
//
//        // 4) (원한다면) 가입 성공 후 이메일 발송 이벤트
//        eventPublisher.publishEvent(new UserSignupEvent(user));
//
//        return user.getId();
//    }
}
