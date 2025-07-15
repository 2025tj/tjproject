// AuthService.java
package com.tj.tjp.security.service;

import com.tj.tjp.dto.auth.login.LoginResult;
import com.tj.tjp.event.EmailVerificationResendEvent;
import com.tj.tjp.event.UserSignupEvent;
import com.tj.tjp.exception.EmailNotVerifiedException;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.service.TokenService;
import com.tj.tjp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        // 이메일 인증 유예기간 체크 및 상태 변경, 메일 재발송
        if (!user.getUser().isEmailVerified()) {
            LocalDateTime expireLimit = user.getUser().getCreatedAt().plusDays(7);
            if (LocalDateTime.now().isAfter(expireLimit)) {
                // 상태 변경
                user.getUser().setStatus("INACTIVE");
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
}
