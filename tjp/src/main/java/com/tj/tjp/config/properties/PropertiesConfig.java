package com.tj.tjp.config.properties;

import com.tj.tjp.config.properties.jwt.JwtProperties;
import lombok.Setter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        FrontendProperties.class,
        CookieProperties.class,
        JwtProperties.class
})
public class PropertiesConfig {
}
