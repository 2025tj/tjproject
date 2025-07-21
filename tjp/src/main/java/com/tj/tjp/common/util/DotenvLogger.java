package com.tj.tjp.common.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Component
public class DotenvLogger implements CommandLineRunner {

    @Override
    public void run(String... args) {
        Logger log = LoggerFactory.getLogger(DotenvLogger.class);

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        log.info("DB_URL: {}", dotenv.get("DB_URL"));
        log.info("DB_USERNAME: {}", dotenv.get("DB_USERNAME"));
        log.info("GOOGLE_CLIENT_ID: {}", dotenv.get("GOOGLE_CLIENT_ID"));
        log.info("GOOGLE_CLIENT_SECRET: {}", dotenv.get("GOOGLE_CLIENT_SECRET"));
        log.info("JWT_SECRET_KEY: {}", dotenv.get("JWT_SECRET_KEY"));
    }
}
