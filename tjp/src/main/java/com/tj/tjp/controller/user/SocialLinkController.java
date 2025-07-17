package com.tj.tjp.controller.user;

import com.tj.tjp.dto.social.SocialLinkRequest;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.service.SocialAccountService;
import com.tj.tjp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialLinkController {

    private final SocialAccountService socialAccountService;
    private final UserService userService;

    /**
     * 소셜 계정 연동 (UserController에서 이동)
     */
    @PostMapping("/link")
    public ResponseEntity<Void> linkSocial(
            @RequestBody SocialLinkRequest req,
            @AuthenticationPrincipal AuthenticatedUser authUser
    ) {
        // 1) 현재 로그인된 User 엔티티 가져오기
        User user = userService.findByEmail(authUser.getEmail())
                .orElseThrow();

        // 2) 서비스에 위임
        socialAccountService.linkSocialAccount(
                user,
                req.getProvider(),
                req.getProviderId()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 소셜 계정 연동 해제
     */
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<Void> unlink(
            @PathVariable String provider,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        try {
            socialAccountService.unlinkSocialAccount(principal.getUser(), provider);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("소셜 계정 연동 해제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 연동된 소셜 프로바이더 목록 조회
     */
    @GetMapping("/linked-providers")
    public ResponseEntity<List<String>> getLinkedProviders(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        List<String> providers = socialAccountService.getLinkedProviders(principal.getUser());
        return ResponseEntity.ok(providers);
    }

//    @GetMapping("/pending-social-link")
//    public ResponseEntity<SocialLinkInfo> pendingLink(
//            @CookieValue(name = "oneTimeLink", required = false) String token
//    ) {
//        Optional<SocialLinkInfo> info = socialAccountService.getPendingLink(token);
//        return info
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.noContent().build());
//    }
//
//    @GetMapping("/pending-social-signup")
//    public ResponseEntity<SocialLinkInfo> pendingSignup(
//            @CookieValue(name = "oneTimeLink", required = false) String token
//    ) {
//        Optional<SocialLinkInfo> info = socialAccountService.getPendingSignup(token);
//        if (info.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        SocialLinkInfo data = info.get();
//        if (socialAccountService.existsLocalUserByEmail(data.email())) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).build();
//        }
//        return ResponseEntity.ok(data);
//    }
//
//    @PostMapping("/link")
//    public ResponseEntity<?> link(
//            @RequestBody OneTimeLinkRequest request,
//            @AuthenticationPrincipal AuthenticatedUser principal
//    ) {
//        socialAccountService.linkWithEmailAndProvider(
//                principal.getUser(), request.email(), request.provider()
//        );
//        return ResponseEntity.ok("소셜 계정 연동 완료");
//    }
//
//    @DeleteMapping("/unlink/{provider}")
//    public ResponseEntity<Void> unlink(
//            @PathVariable String provider,
//            @AuthenticationPrincipal AuthenticatedUser principal
//    ) {
//        socialAccountService.unlink(principal.getUser(), provider);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/linked-providers")
//    public ResponseEntity<List<String>> getLinkedProviders(@AuthenticationPrincipal AuthenticatedUser principal) {
//        List<String> providers = socialAccountService.getLinkedProviders(principal.getUser());
//        return ResponseEntity.ok(providers);
//    }
//
//    @PostMapping("/link-oauth2")
//    public ResponseEntity<Void> linkOAuth2(
//            @RequestBody @Valid SocialLinkRequest req,
//            @AuthenticationPrincipal AuthenticatedUser authUser
//    ) {
//        // 1) 현재 로그인된 User 엔티티 가져오기
//        User user = userService.findByEmail(authUser.getEmail())
//                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
//
//        // 2) 서비스에 위임
//        socialAccountService.linkWithProviderId(
//                user,
//                req.getProvider(),
//                req.getProviderId()
//        );
//
//        return ResponseEntity.ok().build();
//    }

}
