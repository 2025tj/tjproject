package com.tj.tjp.domain.auth.token;

public interface RefreshTokenService {
    // 해당 이메일로 발급된 모든 리프레시 토큰 폐기
    void revokeAllForUser(String email);
}
