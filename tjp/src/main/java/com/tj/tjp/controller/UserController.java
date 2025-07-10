package com.tj.tjp.controller;

import com.tj.tjp.dto.LoginRequest;
import com.tj.tjp.dto.SignupRequest;
import com.tj.tjp.security.JwtProvider;
import com.tj.tjp.security.UserPrincipal;
import com.tj.tjp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tj.tjp.util.CookieUtils.getRefreshToken;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        Long userId = userService.signup(request);
        return ResponseEntity.ok("회원가입 성공. ID: " + userId);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        UserPrincipal user = (UserPrincipal)  authentication.getPrincipal();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", user.getUsername());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 사용자 인증처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 인증된 사용자 정보에서 roles 가져오기
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getUsername();
        var roles = userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();

        // jwt 생성
        String accessToken = jwtProvider.createAccessToken(email, roles);
        String refreshToken = jwtProvider.createRefreshToken(email);

        // jwt를 쿠키로 내려주기
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 운영에서는 treu, https접속 관련
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax") // 프론트, 백 분리인 경우 반드시 None, 개발중엔 Lax
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 사용자 정보 응답
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);

        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email= jwtProvider.getEmailFromToken(refreshToken);
        List<String> roles = jwtProvider.getRolesFromToken(refreshToken); // 만약 refreshToken에 roles가 없다면 기본값으로 대체

        String newAccessToken = jwtProvider.createAccessToken(email, roles != null ? roles : List.of("ROLE_USER"));

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok("Logged out");
    }
}
