package com.tj.tjp.security;

import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.tj.tjp.util.CookieUtils.getAccessToken;
import static com.tj.tjp.util.CookieUtils.getRefreshToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // CORS 응답 헤더 수동 설정(로컬개발용)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        // Preflight (OPTIONS) 요청일 경우 필터 중단
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // jwt 쿠키에서 추출
        String token = getAccessToken(request);

        // authorization헤더에서 토큰 추출 (LocalStorage용)
        if (token == null) {
            String header =request.getHeader("Authorization");
            if (header!=null && header.startsWith("Bearer ")) {
                token =header.substring(7);
            }
        }
        if (token != null) {
            try {
                String email = jwtProvider.getEmailFromToken(token);
//                List<String> roles = jwtProvider.getRolesFromToken(token);

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을수 없음"));

                // 인증 객체 설정
                LocalUserPrincipal principal = new LocalUserPrincipal(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
//
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                logger.warn("JWT 검증 실패: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
