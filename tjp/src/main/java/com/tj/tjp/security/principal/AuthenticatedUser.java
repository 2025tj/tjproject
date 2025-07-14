package com.tj.tjp.security.principal;

import com.tj.tjp.entity.user.User;

public interface AuthenticatedUser {
    String getEmail();
    User getUser();
}
