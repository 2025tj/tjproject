package com.tj.tjp.controller;

import com.tj.tjp.dto.LocalLoginRequest;
import com.tj.tjp.dto.LocalSignupRequest;
import com.tj.tjp.dto.LoginRequest;
import com.tj.tjp.dto.SignupRequest;
import com.tj.tjp.security.AuthenticatedUser;
import com.tj.tjp.security.JwtProvider;
import com.tj.tjp.security.LocalUserPrincipal;
import com.tj.tjp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.tj.tjp.util.CookieUtils.getRefreshToken;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        Long userId = userService.signup(request);
        return ResponseEntity.ok("회원가입 성공. ID: " + userId);
    }

    @PostMapping("/signup/local")
    public ResponseEntity<?> localSignup(@RequestBody @Valid LocalSignupRequest request) {
        Long userId = userService.localSignup(request);
        return ResponseEntity.ok().body("회원가입 성공. userId: " + userId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authenticateAndRespond(request.getEmail(), request.getPassword(), response);
    }

    @PostMapping("/login/local")
    public ResponseEntity<?> loginLocal(@RequestBody @Valid LocalLoginRequest request, HttpServletResponse response) {
        return authenticateAndRespond(request.email(), request.password(), response);
    }

    private ResponseEntity<?> authenticateAndRespond(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AuthenticatedUser userPrincipal = (AuthenticatedUser) authentication.getPrincipal();
        String principalEmail = userPrincipal.getEmail();
        List<String> roles = userPrincipal.getUser().getRoles().stream().toList();

        String accessToken = jwtProvider.createAccessToken(email, roles);
        String refreshToken = jwtProvider.createRefreshToken(email);

        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    private ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(60)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();
    }
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
//        // 사용자 인증처리
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//        );
//
//        // 인증된 사용자 정보에서 roles 가져오기
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        String email = userPrincipal.getUsername();
//        var roles = userPrincipal.getAuthorities().stream()
//                .map(auth -> auth.getAuthority())
//                .toList();
//
//        // jwt 생성
//        String accessToken = jwtProvider.createAccessToken(email, roles);
//        String refreshToken = jwtProvider.createRefreshToken(email);
//
//        // jwt를 쿠키로 내려주기
//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
//                .httpOnly(true)
//                .secure(false) // 운영에서는 treu, https접속 관련
//                .path("/")
//                .maxAge(Duration.ofDays(7))
//                .sameSite("Lax") // 프론트, 백 분리인 경우 반드시 None, 개발중엔 Lax
//                .build();
//
//        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
//
//        // 사용자 정보 응답
//        return ResponseEntity.ok(Map.of("accessToken", accessToken));
//    }

//    @PostMapping("/login/local")
//    public ResponseEntity<?> login(@RequestBody @Valid LocalLoginRequest request, HttpServletResponse response) {
//        //사용자 인증 처리
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.email(), request.password())
//        );
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        String email = userPrincipal.getUsername();
//        List<String> roles = userPrincipal.getAuthorities().stream()
//                .map(auth -> auth.getAuthority())
//                .toList();
//
//        //jwt 발급
//        String accessToken = jwtProvider.createAccessToken(email, roles);
//        String refreshToken = jwtProvider.createRefreshToken(email);
//
//        //refreshToken을 쿠키로 저장
//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
//                .httpOnly(true)
//                .secure(false) // 운영은 true(https)
//                .path("/")
//                .maxAge(Duration.ofDays(7))
//                .sameSite("Lax")
//                .build();
//        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
//        return ResponseEntity.ok(Map.of("accessToken", accessToken));
//
//
//    }

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
