package com.tj.tjp.domain.user.service;

import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenStoreService;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLifecycleService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStoreService refreshTokenStoreService;

    @Transactional
    public void withdraw(String email, String passwordForConfirm, int graceDays) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (passwordForConfirm == null || !passwordEncoder.matches(passwordForConfirm, user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        user.withdraw(graceDays);
    }
}
