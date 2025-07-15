package com.tj.tjp.dto.auth.login;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private final String email;
    private final String message;
    private final String warning; // null이면 경고 없음
}
