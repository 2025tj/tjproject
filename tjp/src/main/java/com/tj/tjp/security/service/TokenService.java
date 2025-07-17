package com.tj.tjp.security.service;

import com.tj.tjp.config.FrontendProperties;
import com.tj.tjp.security.jwt.JwtProvider;
import com.tj.tjp.util.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final FrontendProperties frontendProperties;

    /** Access 토큰만 헤더로 발급 */
    public void issueAccessTokenHeader(HttpServletResponse response, String email, List<String> roles) {
        String accessToken = jwtProvider.createAccessToken(email, roles);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Access-Token");
        response.setHeader("Access-Token", "Bearer " + accessToken);
    }

    /** Refresh 토큰만 HttpOnly 쿠키로 발급 */
    public void issueRefreshTokenCookie(HttpServletResponse response, String email) {
        String refreshToken = jwtProvider.createRefreshToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE,
                String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                        refreshToken, 7*24*60*60));
    }
//    public void issueRefreshTokenCookie(HttpServletResponse response, String email) {
//        String refreshToken = jwtProvider.createRefreshToken(email);
//        response.addHeader(HttpHeaders.SET_COOKIE,
//                String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None; Secure",
//                        refreshToken, 7*24*60*60)
//        );
//    }


    /**
     * 리프레시 토큰 검증 후 새 Access Token 헤더로 발급
     */
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = TokenUtils.getRefreshToken(request);
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or missing refresh token");
        }
        String email = jwtProvider.getEmailFromToken(refreshToken);
        List<String> roles = jwtProvider.getRolesFromToken(refreshToken);
        issueAccessTokenHeader(response, email, roles);
    }

    /**
     * Access Token 검증 (서명·만료 검사)
     * @param token 순수 토큰 문자열
     * @return 유효하면 true
     */
    public boolean validateAccessToken(String token) {
        return jwtProvider.validateToken(token);
    }

    /**
     * Access Token에서 이메일 추출
     */
    public String getEmailFromAccessToken(String token) {
        return jwtProvider.getEmailFromToken(token);
    }

}
