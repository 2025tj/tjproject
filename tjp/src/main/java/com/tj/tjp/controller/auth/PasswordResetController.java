package com.tj.tjp.controller.auth;

import com.tj.tjp.dto.auth.password.PasswordResetExecuteRequest;
import com.tj.tjp.dto.auth.password.PasswordResetRequest;
import com.tj.tjp.dto.common.ApiResponse;
import com.tj.tjp.service.auth.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Password Reset", description = "비밀번호 재설정 관련 API")
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;


    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 링크를 이메일로 발송합니다.")
    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequest request) {
        try {
            log.info("비밀번호 재설정 요청: email={}", request.email());

            passwordResetService.sendPasswordResetEmail(request.email());

            log.info("비밀번호 재설정 메일 발송 완료: email={}", request.email());
            return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 링크가 이메일로 발송되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 요청 실패 - 존재하지 않는 사용자: email={}, message={}",
                    request.email(), e.getMessage());
            // 보안상 사용자 존재 여부를 노출하지 않음
            return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 링크가 이메일로 발송되었습니다."));

        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 실패 - 시스템 오류: email={}, message={}",
                    request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "비밀번호 재설정 요청 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "비밀번호 재설정 토큰 검증", description = "비밀번호 재설정 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Void>> validatePasswordResetToken(
            @RequestParam String token) {
        try {
            log.info("비밀번호 재설정 토큰 검증 요청: token={}", token);

            boolean isValid = passwordResetService.validatePasswordResetToken(token);

            if (isValid) {
                log.info("비밀번호 재설정 토큰 검증 성공: token={}", token);
                return ResponseEntity.ok(ApiResponse.success("유효한 토큰입니다."));
            } else {
                log.warn("비밀번호 재설정 토큰 검증 실패: token={}", token);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("INVALID_TOKEN", "유효하지 않거나 만료된 토큰입니다."));
            }

        } catch (Exception e) {
            log.error("비밀번호 재설정 토큰 검증 실패 - 시스템 오류: token={}, message={}",
                    token, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "토큰 검증 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "비밀번호 재설정 실행", description = "새로운 비밀번호로 재설정합니다.")
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid PasswordResetExecuteRequest request) {
        try {
            log.info("비밀번호 재설정 실행 요청: token={}", request.token());

            passwordResetService.resetPassword(request);

            log.info("비밀번호 재설정 완료: token={}", request.token());
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 실행 실패 - 유효성 검사: token={}, message={}",
                    request.token(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));

        } catch (IllegalStateException e) {
            log.warn("비밀번호 재설정 실행 실패 - 토큰 상태 오류: token={}, message={}",
                    request.token(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("TOKEN_ERROR", e.getMessage()));

        } catch (Exception e) {
            log.error("비밀번호 재설정 실행 실패 - 시스템 오류: token={}, message={}",
                    request.token(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "비밀번호 재설정 처리 중 오류가 발생했습니다."));
        }
    }
}
