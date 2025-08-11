package com.tj.tjp.domain.user.controller;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import com.tj.tjp.domain.user.dto.UpdateUserRequest;
import com.tj.tjp.domain.user.dto.UserResponse;
import com.tj.tjp.domain.user.dto.UserUpdateResult;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.auth.security.principal.AuthenticatedUser;
import com.tj.tjp.domain.user.service.UserLifecycleService;
import com.tj.tjp.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserLifecycleService userLifecycleService;

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인한 사용자의 기본 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCurrentUser(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Map<String, String> userData = Map.of("email", user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    @Operation(summary = "현재 사용자 상세 정보 조회", description = "현재 로그인한 사용자의 상세 정보를 조회합니다.")
    @GetMapping("/me/details")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetails(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        User user = principal.getUser();
        UserResponse dto = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .status(user.getStatus())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "사용자 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResult>> updateUserInfo(
            @RequestBody UpdateUserRequest request,
            Authentication auth
    ) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        UserUpdateResult result = userService.updateUserInfo(user.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 수정되었습니다.", result));
    }

    @Operation(summary = "회원 탈퇴(유예 14일)", description = "비밀번호 확인 후 계정을 비활성화하고 14일 뒤 영구 삭제됩니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal LocalUserPrincipal principal,
            @RequestBody(required = false) Map<String, String> body
            ) {
        String pwd = (body != null) ? body.get("password") : null;
        userLifecycleService.withdraw(principal.getEmail(), pwd, 14);
        return ResponseEntity.ok(ApiResponse.success("탈퇴 처리되었습니다. 14일 후 계정이 영구 삭제됩니다. "));
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
