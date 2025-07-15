package com.tj.tjp.security.service;

import com.tj.tjp.entity.user.User;
import com.tj.tjp.event.EmailVerificationResendEvent;
import com.tj.tjp.exception.EmailNotVerifiedException;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.security.principal.AuthenticatedUser;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void login_emailNotVerifiedAndExpired_shouldSetInactiveAndThrow() {
        // given
        String email = "test@example.com";
        String password = "password";

        // 만료된 유저 생성
        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .roles(Set.of("ROLE_USER"))
                .emailVerified(false)
                .createdAt(LocalDateTime.now().minusDays(10)) // 7일 넘음
                .status("ACTIVE")
                .build();

        AuthenticatedUser authUser = mock(AuthenticatedUser.class);
        when(authUser.getUser()).thenReturn(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(authUser);

        // 인증 매니저가 정상적으로 인증했다고 가정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // when & then
        assertThatThrownBy(() -> authService.login(email, password, response))
                .isInstanceOf(EmailNotVerifiedException.class)
                .hasMessageContaining("이메일 인증 유예기간이 만료되었습니다");

        // user status가 INACTIVE로 변경됐는지
        assertThat(user.getStatus()).isEqualTo("INACTIVE");

        // userRepository.save()가 호출됐는지 확인
        verify(userRepository).save(user);

        // 이벤트가 정상 발행됐는지 확인
        verify(eventPublisher).publishEvent(any(EmailVerificationResendEvent.class));
    }
}