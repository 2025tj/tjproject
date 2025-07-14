package com.tj.tjp.controller.auth;

import com.tj.tjp.dto.auth.login.LocalLoginRequest;
import com.tj.tjp.dto.auth.singup.SignupRequest;
import com.tj.tjp.security.service.AuthService;
import com.tj.tjp.security.service.TokenService;
import com.tj.tjp.service.UserService;
import com.tj.tjp.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        Long userId = userService.signup(request);
        return ResponseEntity.ok("회원가입 성공. userId: " + userId);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> loginLocal(
            @RequestBody @Valid LocalLoginRequest request,
            HttpServletResponse response
    ) {
        authService.login(request.email(), request.password(), response);
        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
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
}

