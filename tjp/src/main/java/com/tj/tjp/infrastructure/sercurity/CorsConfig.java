package com.tj.tjp.infrastructure.sercurity;

import com.tj.tjp.infrastructure.config.properties.FrontendProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final FrontendProperties frontendProperties;

    @PostConstruct
    public void init() {
        List<String> origins = frontendProperties.getAllowedOrigins();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // 프로퍼티에서 읽어온 allowedOrigins 리스트를 배열로 변환
        String[] origins = frontendProperties
                .getAllowedOrigins()
                .toArray(new String[0]);

        registry.addMapping("/**")
                // 프론트엔드 도메인 (개발용)
                .allowedOrigins(origins)
                // 요청 가능한 HTTP 메서드
                .allowedMethods("*")
                // 클라이언트가 요청 시 사용할 수 있는 헤더
                .allowedHeaders("*")
                // 응답 헤더 중 브라우저 JS에서 열람할 헤더
                .exposedHeaders("Access-Token", "Refresh-Token")
                // 쿠키·자격증명 사용 여부 (여기서는 토큰 헤더라 false로 해도 무방)
                .allowCredentials(true);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(frontendProperties.getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Access-Token", "Refresh-Token"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
