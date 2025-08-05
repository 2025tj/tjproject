package com.tj.tjp.infrastructure.config;

import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.entity.UserStatus;
import com.tj.tjp.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class AdminAccountInitializer {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            String adminEmail = System.getProperty("ADMIN_EMAIL");
            if (adminEmail == null || adminEmail.isBlank()) {
                adminEmail = System.getenv("ADMIN_EMAIL");
            }
            String adminPassword = System.getProperty("ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.isBlank()) {
                adminPassword = System.getenv("ADMIN_PASSWORD");
            }
            if (adminEmail == null || adminPassword == null) {
                log.warn("ADMIN_EMAIL 또는 ADMIN_PASSWORD 환경변수가 설정되지 않아 관리자 계정을 생성하지 않습니다.");
                return;
            }
            Optional<User> existing = userRepository.findByEmail(adminEmail);
            if (existing.isPresent()) {
                log.info("관리자 계정이 이미 존재합니다.");
            } else {
                User admin = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .nickname("관리자")
                        .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                        .build();
                admin.verifyEmail();
                admin.activate();
                userRepository.save(admin);
                log.info("관리자 계정 생성 완료: {}", adminEmail);
            }
        };
    }
}
