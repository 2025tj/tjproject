package com.tj.tjp.domain.user.service;

import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenStoreService;
import com.tj.tjp.domain.auth.token.RefreshTokenService;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.entity.UserStatus;
import com.tj.tjp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLifecycleService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void withdraw(String email, String passwordForConfirm, int graceDays) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (passwordForConfirm == null || !passwordEncoder.matches(passwordForConfirm, user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        // TODO: 구독/ 환불 진행중 등 제약 있으면 코드 추가

        user.withdraw(graceDays);
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getEmail());
    }

    // 탈퇴 취소(복구)
    @Transactional
    public void cancelWithdrawal(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.cancelWithdrawal();
    }

    // 탈퇴 예정 사용자 완전 삭제
    @Transactional
    public int hardDeleteDueUsers() {
        List<User> targets =userRepository.findDueForHardDelete(LocalDateTime.now());
        for (User u : targets) {
            // TODO: 연관 데이터 익명화/해제/set null 처리 필요
            userRepository.delete(u);
        }
        return targets.size();
    }

    @Transactional
    public void reactivateByCredential(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 상태확인
        if (!UserStatus.INACTIVE.name().equalsIgnoreCase(user.getStatus())) {
            return;
        }

        // 비번 검증
//        if (user.getPassword() == null || user.getPassword().isBlank()) {
//            throw new IllegalArgumentException("소셜 가입계정입니다. 소셜 로그인 후 복구를 진행하세요.");
//        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        user.cancelWithdrawal();
    }
}
