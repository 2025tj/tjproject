package com.tj.tjp.domain.auth.blacklist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenInfo {
    private String email;
    private String key;
    private String hashPreview;
    private Long ttlMillis;
}
