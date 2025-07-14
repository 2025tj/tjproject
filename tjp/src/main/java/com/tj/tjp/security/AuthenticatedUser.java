package com.tj.tjp.security;

import com.tj.tjp.entity.User;

public interface AuthenticatedUser {
    String getEmail();
    User getUser();
}
