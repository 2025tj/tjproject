package com.tj.tjp.domain.admin.controller;

import com.tj.tjp.domain.auth.blacklist.dto.RefreshTokenInfo;
import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refresh-tokens")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefreshTokenController {
    private final RefreshTokenAdminService svc;

    @GetMapping
    public ResponseEntity<List<RefreshTokenInfo>> list(
            @RequestParam(required = false) String pattern,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "1000") int scanCount
    ) {
//        return svc.listTokens(pattern, limit, scanCount);
        return ResponseEntity.ok(svc.listTokens(pattern, limit, scanCount));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteOne(@PathVariable String email) {
//        return svc.deleteByEmail(email)? "delated" : "not found";
        return svc.deleteByEmail(email)
                ? ResponseEntity.noContent().build() // 204
                : ResponseEntity.notFound().build(); // 404
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteByPattern(
            @RequestParam(required = false) String pattern,
            @RequestParam(defaultValue = "1000") int scanCount
    ) {
//        int n = svc.deleteByPattern(pattern, scanCount);
//        return "deleted="+n;
        svc.deleteByPattern(pattern, scanCount);
        return ResponseEntity.noContent().build(); // 204
    }
}
