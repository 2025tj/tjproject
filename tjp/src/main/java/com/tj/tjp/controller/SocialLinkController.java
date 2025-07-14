package com.tj.tjp.controller;

import com.tj.tjp.entity.ProviderType;
import com.tj.tjp.security.LinkableOAuth2UserPrincipal;
import com.tj.tjp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SocialLinkController {

    private final UserService userService;

    @GetMapping("/pending-social-link")
    public ResponseEntity<?> getPendingLink(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof LinkableOAuth2UserPrincipal principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("연동 대상이 아닙니다.");
        }
        return ResponseEntity.ok(Map.of(
                "email", principal.getEmail(),
                "provider", principal.getProvider()
        ));
    }

    @PostMapping("/link-social")
    public ResponseEntity<?> link(@RequestBody SocialLinkRequest request) {
        userService.linkSocialAccount(request.email(), request.provider);
        return ResponseEntity.ok("연동 완료");
    }

    public record SocialLinkRequest(String email, String provider) {}
}
