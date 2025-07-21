package com.tj.tjp.service.email;

import com.tj.tjp.domain.email.service.EmailVerificationService;
import com.tj.tjp.domain.email.service.MailSenderService;
import com.tj.tjp.domain.email.entity.EmailVerificationToken;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.email.repository.EmailVerificationTokenRepository;
import com.tj.tjp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class EmailVerificationServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private EmailVerificationService emailVerificationService;

    // 메일 발송은 실제로 보내지 않게 Mock 처리!
    @MockitoBean
    private MailSenderService mailSenderService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@email.com")
                .password("pw")
                .nickname("tester")
                .roles(new HashSet<>(Set.of("ROLE_USER")))
                .status("ACTIVE")
                .build();
        user = userRepository.save(user);
    }

    @Test
    void 이메일인증_전체플로우_정상() {
        // 인증 메일 발송 (토큰 생성, 저장, 메일 발송)
        emailVerificationService.sendVerificationEmail(user);

        // 메일 발송이 실제로 호출됐는지 Mock으로 검증
        verify(mailSenderService, atLeastOnce()).send(anyString(), anyString(), anyString(), anyBoolean());

        // 토큰이 DB에 저장되었는지 검증
        EmailVerificationToken tokenEntity = tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow();
        assertNotNull(tokenEntity);
        assertFalse(tokenEntity.isUsed());

        // 인증 처리
        emailVerificationService.verifyEmailToken(tokenEntity.getToken());

        // User의 emailVerified가 true로 변경됐는지 확인
        User verifiedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(verifiedUser.isEmailVerified());

        // 토큰이 사용 처리됐는지 확인
        EmailVerificationToken usedToken = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
        assertTrue(usedToken.isUsed());
    }

    @Test
    void 잘못된토큰_예외_검증() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailVerificationService.verifyEmailToken("invalid-token");
        });
    }

    // 만료된 토큰, 이미 사용된 토큰 등도 아래처럼 테스트 가능!
    @Test
    void 만료된토큰_예외_검증() {
        // 토큰을 강제로 만료시킴
        emailVerificationService.sendVerificationEmail(user);
        EmailVerificationToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow();

        // 토큰을 만료시각으로 조작
        token.extendExpiry(token.getExpiredAt().minusDays(2));
        tokenRepository.save(token);

        assertThrows(IllegalStateException.class, () -> {
            emailVerificationService.verifyEmailToken(token.getToken());
        });
    }

    @Test
    void 이미사용된토큰_예외_검증() {
        emailVerificationService.sendVerificationEmail(user);
        EmailVerificationToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow();

        // 인증 1회 성공
        emailVerificationService.verifyEmailToken(token.getToken());
        // 2회 인증 시도
        assertThrows(IllegalStateException.class, () -> {
            emailVerificationService.verifyEmailToken(token.getToken());
        });
    }
}