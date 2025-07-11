package com.tj.tjp.controller;

import com.tj.tjp.dto.*;
import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import com.tj.tjp.security.JwtProvider;
import com.tj.tjp.security.UserPrincipal;
import com.tj.tjp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tj.tjp.util.CookieUtils.getRefreshToken;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        UserPrincipal user = (UserPrincipal)  authentication.getPrincipal();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", user.getUsername());
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserRequest request, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        userService.updateUserInfo(principal.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/link-social")
    public ResponseEntity<?> linkSocial(@RequestBody LinkSocialRequest request) {
        try {
            userService.linkSoicalAccount(request.getEmail(), request.getProvider());
            return ResponseEntity.ok("소셜 연동 완료");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
