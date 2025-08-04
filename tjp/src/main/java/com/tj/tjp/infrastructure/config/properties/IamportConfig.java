package com.tj.tjp.infrastructure.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@ConfigurationProperties(prefix = "iamport")
public class IamportConfig {
    private String apiKey;
    private String apiSecret;



}
