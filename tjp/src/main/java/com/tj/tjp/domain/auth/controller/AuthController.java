package com.tj.tjp.domain.auth.controller;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.common.exception.EmailNotVerifiedException;
import com.tj.tjp.domain.auth.dto.login.LoginRequest;
import com.tj.tjp.domain.auth.dto.login.LoginResult;
import com.tj.tjp.domain.auth.dto.signup.SignupRequest;
import com.tj.tjp.domain.auth.security.principal.AuthenticatedUser;
import com.tj.tjp.domain.auth.service.AuthService;
import com.tj.tjp.domain.auth.security.service.TokenService;
import com.tj.tjp.domain.auth.service.PasswordResetService;
import com.tj.tjp.domain.email.service.EmailVerificationService;
import com.tj.tjp.domain.user.service.UserService;
import com.tj.tjp.common.util.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signup(@RequestBody @Valid SignupRequest request) {
        try {
            log.info("회원가입 요청: email={}, nickname={}", request.getEmail(), request.getNickname());

            Long userId = userService.signup(request);

            log.info("회원가입 성공: userId={}", userId);
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", userId));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 - 유효성 검사: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 실패 - 시스템 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "로그인", description = "사용자 인증을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> loginLocal(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse response
    ) {
        try {
            LoginResult result = authService.login(request.email(), request.password(), httpServletRequest, response);
            //AuthService에서 저장한 메세지 읽기
            String message = (String) RequestContextHolder.currentRequestAttributes()
                    .getAttribute("loginMessage", RequestAttributes.SCOPE_REQUEST);
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
        } catch (EmailNotVerifiedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("EMAIL_NOT_VERIFIED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "토큰 갱신", description = "액세스 토큰을 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        tokenService.refreshAccessToken(request, response);
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신이 완료되었습니다."));
    }


    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedUser userPrincipal,
            HttpServletRequest request,
            HttpServletResponse response) {
        tokenService.deleteRefreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }

    /**
     * 로컬 accessToken 검증용 (서버 서명·유효기간·블랙리스트 검사)
     */
    @Operation(summary = "토큰 검증", description = "액세스 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validate(HttpServletRequest request) {
        String token = TokenUtils.getAccessToken(request);
        if (token == null || !tokenService.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("INVALID_TOKEN", "유효하지 않은 토큰입니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("토큰이 유효합니다."));
    }

    // 응답 본문은 필요 없음. 헤더만 내려줄 목적
    @Operation(summary = "OAuth2 완료", description = "OAuth2 인증 완료를 처리합니다.")
    @GetMapping("/oauth2/complete")
    public ResponseEntity<ApiResponse<Void>> oauth2Complete() {
        return ResponseEntity.ok(ApiResponse.success("OAuth2 인증이 완료되었습니다."));
    }
}

