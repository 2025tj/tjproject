package com.tj.tjp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {
    /**
     * application.yml에서
     * frontend:
     *   redirect-urls:
     *     - signup:
     *     - oauth2:
     *   allowed-origins:
     *     - http://localhost:5173
     *     - https://domainaddress.com
     */
    private Map<String, String> redirectUrls;
    private List<String> allowedOrigins;;
}
