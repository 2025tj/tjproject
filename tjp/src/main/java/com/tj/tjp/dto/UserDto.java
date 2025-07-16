package com.tj.tjp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String nickname;
    private Set<String> roles;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private String status;
}
