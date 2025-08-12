package com.tj.tjp.domain.auth.token;

import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenAdminService;
import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService{
    private final RefreshTokenStoreService store; // 단일 사용자 RfreshToken 저장/삭제
    private final RefreshTokenAdminService admin; // 패턴 삭제 등 관리용 (옵션)

    @Override
    public void revokeAllForUser(String email) {
        // 현재 구조상 이메일 1:1 키이므로 이 한 줄이면 모든 디바이스 로그아웃
        store.delete(email);
    }

    // (필요시 사용) 운영자가 전채/패턴 삭제
    public int revokeByPattern(String pattern, int scanCount) {
        return admin.deleteByPattern(pattern, scanCount);
    }

}
