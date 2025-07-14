package com.tj.tjp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {
    /**
     * application.yml에서
     * frontend:
     *   allowed-origins:
     *     - http://localhost:5173
     *     - https://domainaddress.com
     */
    private String redirectUrl;
    private List<String> allowedOrigins;;
}
