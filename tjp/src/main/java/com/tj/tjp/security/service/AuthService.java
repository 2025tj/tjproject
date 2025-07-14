// AuthService.java
package com.tj.tjp.security.service;

import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    /**
     * 일반 로그인(email/password) 처리
     */
    public void login(String email, String password, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // 1) 액세스 토큰은 헤더에만
        tokenService.issueAccessTokenHeader(response, user.getEmail(), roles);

        // 2) 리프레시 토큰은 HttpOnly 쿠키에만
        tokenService.issueRefreshTokenCookie(response, user.getEmail());
    }
}
