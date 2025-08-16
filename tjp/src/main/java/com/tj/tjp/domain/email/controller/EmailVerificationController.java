package com.tj.tjp.domain.email.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tj.tjp.domain.email.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("controller.verifyEmail : token ={}", token);
        try {
            emailVerificationService.verifyEmailToken(token);
            log.info("verifyEmailToken : token ={}", token);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (Exception e) {
            log.error("controller.verifyEmail : token ={}", token, e.getMessage(), e);
            return ResponseEntity.badRequest().body("인증에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이메일 인증 메일 재발송 엔드포인트
     * - 로그인한 사용자가 이메일 인증을 다시 받고자 할 때 호출
     * - 기존 토큰 무효화 후 새로운 토큰 생성 및 메일 발송
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 재발송 성공: 200 OK + 메시지, 실패: 400 Bad Request + 에러 메시지
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            emailVerificationService.resendVerificationEmail(userDetails.getUsername());
            return ResponseEntity.ok("인증 메일이 재발송되었습니다. 이메일을 확인해주세요.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("재발송에 실패했습니다: " + e.getMessage());
        }
    }
}
