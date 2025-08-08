package com.tj.tjp.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
        prePostEnabled = true, // @PreAuthorize / @PostAuthorize 사용
        securedEnabled = true, // @Secured 사용(원하면)
        jsr250Enabled = false // @RolesAllowed 안쓰면 false
)
public class MethodSecurityConfig {
}
