package com.tj.tjp.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix="app.cookie")
public class CookieProperties {
    private boolean secure;
    private String sameSite;
}
