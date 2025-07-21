package com.tj.tjp;

import com.tj.tjp.infrastructure.config.properties.FrontendProperties;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@EnableConfigurationProperties(FrontendProperties.class)
@SpringBootApplication
public class TjpApplication {

	static {
		// 🚀 환경변수 자동 로딩
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			// 모든 환경변수를 시스템 프로퍼티로 자동 등록
			int count = 0;
			for (var entry : dotenv.entries()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (System.getProperty(key) == null && value != null) {
					System.setProperty(key, value);
					count++;
				}
			}

			// 성공 메시지 출력 (로그 대신 System.out 사용)
			System.out.println("@@@ Environment variables loaded from .env file (" + count + "개)");

		} catch (Exception e) {
			System.out.println("@@@ Failed to load .env file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(TjpApplication.class, args);
		log.info("@@@ TJP Application started successfully!");
		System.out.print("Hello tj!");
	}

}
