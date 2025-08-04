package com.tj.tjp.infrastructure.config.properties;

import com.tj.tjp.infrastructure.config.properties.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        FrontendProperties.class,
        CookieProperties.class,
        JwtProperties.class,
        IamportConfig.class,
})
public class PropertiesConfig {
}
