package com.tj.tjp.service.email;

import com.tj.tjp.config.properties.FrontendProperties;
import com.tj.tjp.entity.user.EmailVerificationToken;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.repository.user.EmailVerificationTokenRepository;
import com.tj.tjp.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * 이메일 인증 서비스
 * - 회원가입 시 인증 메일 발송
 * - 인증 토큰 생성/저장/검증 등 이메일 인증 전체 흐름 담당
 */
@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final FrontendProperties frontendProperties;


    /**
     * 회원가입 직후 호출
     * - 이메일 인증 토큰 생성 및 저장
     * - 인증 URL 생성 후 사용자 이메일로 발송
     * @param user 이메일 인증이 필요한 신규 회원
     */
    public void sendVerificationEmail(User user) {
        // 1. 토큰 생성 (랜덤 UUID, 24시간 유효)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(24);

        // 2. 토큰 DB 저장
        EmailVerificationToken tokenEntity = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiredAt(expiredAt)
                .build();
        try {
            tokenRepository.save(tokenEntity);
        } catch (Exception e) {
            // DB 장애 등 예외 처리 (로깅 및 재시도/관리자 알림 등)
            throw new RuntimeException("인증 토큰 저장에 실패했습니다.", e);
        }

        // 3. 인증 링크 생성 (프론트엔드 URL 활용)
        String verificationUrl = frontendProperties.getRedirectUrls().get("emailVerification") + "?token=" + token;

        // 4. 인증 메일 발송
        sendVerificationMail(user.getEmail(), verificationUrl);
    }

    /**
     * 이메일 인증 메일 재발송
     * - 기존 미사용 토큰들을 무효화하고 새로운 토큰 생성
     * - 새로운 인증 메일 발송
     * @param email 재발송을 요청한 사용자의 이메일
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 이미 인증된 사용자인지 확인
        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료된 사용자입니다.");
        }

        // 3. 기존 미사용 토큰들 무효화 (사용 처리)
        List<EmailVerificationToken> existingTokens = tokenRepository.findByUserAndUsedFalse(user);
        existingTokens.forEach(token -> token.markUsed(true));
        tokenRepository.saveAll(existingTokens);

        // 4. 새로운 토큰 생성 및 저장
        String newToken = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(24);

        EmailVerificationToken newTokenEntity = EmailVerificationToken.builder()
                .token(newToken)
                .user(user)
                .expiredAt(expiredAt)
                .build();

        try {
            tokenRepository.save(newTokenEntity);
        } catch (Exception e) {
            throw new RuntimeException("인증 토큰 저장에 실패했습니다.", e);
        }

        // 5. 새로운 인증 링크 생성 및 메일 발송
        String verificationUrl = frontendProperties.getRedirectUrls().get("emailVerification") + "?token=" + newToken;
        sendVerificationMail(user.getEmail(), verificationUrl);
    }

    /**
     * 이메일 인증 토큰 검증 및 인증 처리
     * - 인증 링크 클릭시 호출 (ex: /auth/verify?token=xxxx)
     * - 토큰 만료/사용 여부 검증 및 유저 인증 처리
     * @param token 인증 URL의 쿼리파라미터로 전달된 토큰 값
     */
    @Transactional
    public void verifyEmailToken(String token) {
        // 1. 토큰 조회 및 유효성 검증
        EmailVerificationToken emailVerificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 토큰입니다."));

        if (emailVerificationToken.isUsed() || emailVerificationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("토큰이 만료되었거나 이미 사용되었습니다.");
        }

        // 2. 유저 정보 가져오기 (토큰과 연관된 유저)
        User user = userRepository.findById(emailVerificationToken.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 3. 인증 처리 (이메일 인증 완료)
        user.verifyEmail(); // emailVerified=true, emailVerifiedAt=now()
        userRepository.save(user);

        // 4. 토큰 사용 처리 (1회성)
        emailVerificationToken.markUsed(true);
        tokenRepository.save(emailVerificationToken);
    }

    /**
     * 인증 메일 발송 공통 메서드
     * @param email 수신자 이메일
     * @param verificationUrl 인증 URL
     */
    private void sendVerificationMail(String email, String verificationUrl) {
        String subject = "[서비스명] 이메일 인증을 완료해주세요";
        String message = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #333;'>이메일 인증</h2>"
                + "<p>안녕하세요!</p>"
                + "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + verificationUrl + "' style='background-color: #007bff; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>이메일 인증하기</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 14px;'>만약 버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>"
                + "<p style='word-break: break-all; color: #666; font-size: 14px;'>" + verificationUrl + "</p>"
                + "<p style='color: #666; font-size: 12px; margin-top: 30px;'>이 링크는 24시간 후 만료됩니다.</p>"
                + "</div>";

        try {
            mailSenderService.send(email, subject, message, true);
        } catch (Exception e) {
            throw new RuntimeException("이메일 인증 메일 발송에 실패했습니다.", e);
        }
    }
}
