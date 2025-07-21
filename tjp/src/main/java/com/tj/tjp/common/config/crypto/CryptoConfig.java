package com.tj.tjp.common.config.crypto;

import com.tj.tjp.common.util.CryptoUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

    @Bean
    public CryptoUtils cryptoUtils() {
        return new CryptoUtils();
    }
}