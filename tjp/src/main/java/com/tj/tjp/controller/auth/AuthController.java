package com.tj.tjp.controller.auth;

import com.tj.tjp.dto.auth.login.LoginRequest;
import com.tj.tjp.dto.auth.login.LoginResult;
import com.tj.tjp.dto.auth.singup.SignupRequest;
import com.tj.tjp.security.service.AuthService;
import com.tj.tjp.security.service.TokenService;
import com.tj.tjp.service.email.EmailVerificationService;
import com.tj.tjp.service.user.UserService;
import com.tj.tjp.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        Long userId = userService.signup(request);
        return ResponseEntity.ok("회원가입 성공. userId: " + userId);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResult> loginLocal(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request.email(), request.password(), response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String,String>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            tokenService.refreshAccessToken(request, response);
            return ResponseEntity.ok(Map.of("message", "토큰 재발급 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout(HttpServletResponse response) {
        // refreshToken 쿠키 삭제 (name은 실제 발급명과 동일해야 함)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // 운영 환경에서만 true!
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    /**
     * 로컬 accessToken 검증용 (서버 서명·유효기간·블랙리스트 검사)
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(HttpServletRequest request) {
        // 헤더 혹은 쿠키(Fallback)에서 accessToken 추출
        String token = TokenUtils.getAccessToken(request);
        if (token == null || !tokenService.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }

    // 응답 본문은 필요 없음. 헤더만 내려줄 목적
    @GetMapping("/oauth2/complete")
    public ResponseEntity<Void> oauth2Complete() {
        return ResponseEntity.ok().build();
    }

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

