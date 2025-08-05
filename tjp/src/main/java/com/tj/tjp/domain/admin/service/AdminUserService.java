package com.tj.tjp.domain.admin.service;

import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AdminUserService {
    private final UserRepository userRepository;

    // 전체 사용자 목록 조회
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // 사용자 차단
    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.block();
    }

    // 사용자 활성화
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.activate();
    }

    // 사용자 비활성화
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.deactivate();
    }
}
