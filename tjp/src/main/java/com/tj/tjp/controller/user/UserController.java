package com.tj.tjp.controller.user;

import com.tj.tjp.dto.user.UpdateUserRequest;
import com.tj.tjp.dto.user.UserUpdateResult;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.jwt.JwtProvider;
import com.tj.tjp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser)  authentication.getPrincipal();

        return ResponseEntity.ok(Map.of("email",user.getEmail()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserUpdateResult> updateUserInfo(@RequestBody UpdateUserRequest request, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        UserUpdateResult result = userService.updateUserInfo(user.getEmail(), request);
        return ResponseEntity.ok(result);
    }

//    @PostMapping("/link-social")
//    public ResponseEntity<?> linkSocial(@RequestBody LinkSocialRequest request) {
//        try {
//            userService.linkSocialAccount(request.getEmail(), request.getProvider());
//            return ResponseEntity.ok("소셜 연동 완료");
//        } catch (IllegalArgumentException | IllegalStateException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
