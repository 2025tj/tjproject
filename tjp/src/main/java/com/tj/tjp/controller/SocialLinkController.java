package com.tj.tjp.controller;

import com.tj.tjp.security.service.TokenService;
import com.tj.tjp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SocialLinkController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * 프론트에서 소셜 연동 유도 시 사용자 정보 요청
     */
//    @GetMapping("/pending-social-link")
//    public ResponseEntity<?> getPendingLink(Authentication auth) {
//        if (auth == null || !(auth.getPrincipal() instanceof LinkableOAuth2UserPrincipal principal)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("연동 대상이 아닙니다.");
//        }
//        return ResponseEntity.ok(Map.of(
//                "email", principal.getEmail(),
//                "provider", principal.getProvider().name().toLowerCase()
//        ));
//    }

    /**
     * 사용자가 연동을 수락했을 때 실제 연동 처리 및 토큰 발급
     */
//    @PostMapping("/link-social")
//    public ResponseEntity<?> link(@RequestBody SocialLinkRequest request, HttpServletResponse response) {
//        // 연동 처리
//        User user = userService.linkSocialAccount(request.email(), request.provider);
//
//        tokenService.issueAccessTokenHeader(response, user.getEmail(), user.getRoles().stream().toList());
//        tokenService.issueRefreshTokenCookie(response, user.getEmail());
//
//        return ResponseEntity.ok(Map.of("message", "연동 완료"));
//    }
//
//    public record SocialLinkRequest(String email, ProviderType provider) {}
}
