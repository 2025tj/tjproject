package com.tj.tjp.domain.auth.security.filter;

import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.common.exception.EmailNotVerifiedException;
import com.tj.tjp.domain.user.repository.UserRepository;
import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import com.tj.tjp.domain.auth.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // JWT 생성/검증을 담당하는 컴포넌트
    private final JwtProvider jwtProvider;

    // 사용자 조회를 위한 리포지토리
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        // 1) Preflight 요청은 인증 없이 통과시킵니다.(필터 중단)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2) Authorization 헤더에서 "Bearer " 접두사가 붙은 토큰을 추출
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);  // "Bearer " 이후 부분이 실제 토큰
        }

        // 3) 토큰이 존재하면 검증 및 인증 처리
        if (token != null && jwtProvider.validateToken(token)) {
            try {
                // 토큰에서 이메일(주체)을 추출
                String email = jwtProvider.getEmailFromToken(token);
                // DB에서 사용자 정보 조회
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없음"));

                // 이메일 미인증시 유예기간 체크
                if (!user.isEmailVerified()) {
                    LocalDateTime expireLimit = user.getCreatedAt().plusDays(7);
                    if (LocalDateTime.now().isAfter(expireLimit)) {
                        // 예외 던지면 아래 인증 로직 건너뜀 (권장!)
                        throw new EmailNotVerifiedException("이메일 인증 유예기간이 만료되었습니다. 로그인 불가. 인증메일 재전송 필요");
                    }
                    // 유예기간 내라면 경고만 로그 등으로 남기고 로그인 허용
                    // 경고 메세지 포함해서 내려줌(예: 헤더 or 바디)
                }

                // Spring Security 인증 객체 생성
                LocalUserPrincipal principal = new LocalUserPrincipal(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
                // 요청 상세 정보 설정 (IP, 세션 등)
                authentication.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("JWT 검증 실패: {}", e.getMessage());
            }
        }

        // 4) 나머지 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
