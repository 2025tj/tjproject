package com.tj.tjp.event;


import com.tj.tjp.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailVerificationResendEvent {
    private final User user;
}
