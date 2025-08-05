package com.tj.tjp.domain.admin.controller;

import com.tj.tjp.domain.admin.service.AdminUserService;
import com.tj.tjp.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 전체 사용자 조회
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.findAllUsers());
    }

    // 사용자 차단
    @PatchMapping("/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok("사용자가 차단되었습니다.");
    }

    // 사용자 활성화
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        adminUserService.activateUser(id);
        return ResponseEntity.ok("사용자가 활성화되었습니다.");
    }

    // 사용자 비활성화
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        adminUserService.deactivateUser(id);
        return ResponseEntity.ok("사용자가 비활성화되었습니다.");
    }
}
