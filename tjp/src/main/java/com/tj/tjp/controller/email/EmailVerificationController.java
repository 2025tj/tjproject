package com.tj.tjp.controller.email;

import com.tj.tjp.service.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 토큰 검증 엔드포인트
     * - 인증 메일의 링크 클릭시 호출되는 API
     * - 토큰 검증 후 이메일 인증 처리(성공/실패 결과 반환)
     *
     * @param token 인증 URL에 포함된 쿼리파라미터(랜덤 토큰)
     * @return 인증 성공: 200 OK + 메시지, 실패: 400 Bad Request + 에러 메시지
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyEmailToken(token);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증에 실패했습니다: " + e.getMessage());
        }
    }
}
