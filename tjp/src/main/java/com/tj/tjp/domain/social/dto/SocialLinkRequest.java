package com.tj.tjp.domain.social.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SocialLinkRequest {
    @NotBlank
    private String provider;
    @NotBlank// "google", "kakao"
    private String providerId;   // 소셜 고유 ID
    private String accessToken;  // 선택: 카카오 친구 목록 등 활용 시 필요
}
