package com.tj.tjp.service.auth;

import com.tj.tjp.config.properties.FrontendProperties;
import com.tj.tjp.dto.auth.password.PasswordResetExecuteRequest;
import com.tj.tjp.entity.user.PasswordResetToken;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.repository.user.PasswordResetTokenRepository;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.service.email.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final FrontendProperties frontendProperties;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 메일 발송
     * @param email 비밀번호 재설정을 요청한 사용자의 이메일
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 기존 미사용 토큰들 무효화
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUserAndUsedFalse(user);
        existingTokens.forEach(token -> token.markUsed(true));
        passwordResetTokenRepository.saveAll(existingTokens);

        // 3. 새로운 토큰 생성 (1시간 유효)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiredAt(expiredAt)
                .build();

        try {
            passwordResetTokenRepository.save(resetToken);
            log.info("비밀번호 재설정 토큰 생성 완료: email={}, token={}", email, token);
        } catch (Exception e) {
            log.error("비밀번호 재설정 토큰 저장 실패: email={}", email, e);
            throw new RuntimeException("비밀번호 재설정 토큰 저장에 실패했습니다.", e);
        }

        // 4. 비밀번호 재설정 링크 생성 및 메일 발송
        String resetUrl = frontendProperties.getRedirectUrls().get("passwordReset") + "?token=" + token;
        sendPasswordResetMail(user.getEmail(), resetUrl);
    }

    /**
     * 비밀번호 재설정 토큰 검증
     * @param token 비밀번호 재설정 토큰
     * @return 토큰이 유효한지 여부
     */
    public boolean validatePasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isUsed() && !resetToken.isExpired())
                .orElse(false);
    }

    /**
     * 비밀번호 재설정 실행
     * @param request 비밀번호 재설정 요청 정보
     */
    @Transactional
    public void resetPassword(PasswordResetExecuteRequest request) {
        // 1. 비밀번호 확인 검증
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 토큰 조회 및 유효성 검증
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 비밀번호 재설정 토큰입니다."));

        if (resetToken.isUsed()) {
            throw new IllegalStateException("이미 사용된 토큰입니다.");
        }

        if (resetToken.isExpired()) {
            throw new IllegalStateException("만료된 토큰입니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        // 3. 사용자 정보 가져오기
        User user = resetToken.getUser();

        // 4. 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // 5. 토큰 사용 처리
        resetToken.markUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("비밀번호 재설정 완료: email={}", user.getEmail());
    }

    /**
     * 비밀번호 재설정 메일 발송
     * @param email 수신자 이메일
     * @param resetUrl 비밀번호 재설정 URL
     */
    private void sendPasswordResetMail(String email, String resetUrl) {
        String subject = "[서비스명] 비밀번호 재설정";
        String message = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #333;'>비밀번호 재설정</h2>"
                + "<p>안녕하세요!</p>"
                + "<p>비밀번호 재설정을 요청하셨습니다.</p>"
                + "<p>아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + resetUrl + "' style='background-color: #dc3545; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>비밀번호 재설정하기</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 14px;'>만약 버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>"
                + "<p style='word-break: break-all; color: #666; font-size: 14px;'>" + resetUrl + "</p>"
                + "<p style='color: #666; font-size: 12px; margin-top: 30px;'>이 링크는 1시간 후 만료됩니다.</p>"
                + "<p style='color: #666; font-size: 12px;'>만약 비밀번호 재설정을 요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.</p>"
                + "</div>";

        try {
            mailSenderService.send(email, subject, message, true);
            log.info("비밀번호 재설정 메일 발송 완료: email={}", email);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패: email={}", email, e);
            throw new RuntimeException("비밀번호 재설정 메일 발송에 실패했습니다.", e);
        }
    }
}
