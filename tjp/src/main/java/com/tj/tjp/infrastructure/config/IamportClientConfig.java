package com.tj.tjp.infrastructure.config;

import com.siot.IamportRestClient.IamportClient;
import com.tj.tjp.infrastructure.config.properties.IamportConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class IamportClientConfig {
    private final IamportConfig iamportConfig;

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(
                iamportConfig.getApiKey(),
                iamportConfig.getApiSecret()
        );
    }
}
