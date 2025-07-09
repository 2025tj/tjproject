package com.tj.tjp.controller;

import com.tj.tjp.dto.SignupRequest;
import com.tj.tjp.security.UserPrincipal;
import com.tj.tjp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        Long userId = userService.signup(request);
        return ResponseEntity.ok("회원가입 성공. ID: " + userId);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        UserPrincipal user = (UserPrincipal)  authentication.getPrincipal();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", user.getUsername());
        return ResponseEntity.ok(userInfo);
    }
}
