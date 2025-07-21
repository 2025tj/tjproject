package com.tj.tjp.domain.auth.security.principal;

import com.tj.tjp.domain.user.entity.User;

public interface AuthenticatedUser {
    String getEmail();
    User getUser();
}
