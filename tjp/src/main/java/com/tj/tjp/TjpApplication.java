package com.tj.tjp;

import com.tj.tjp.config.FrontendProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(FrontendProperties.class)
@SpringBootApplication
public class TjpApplication {
	private static final Logger log = LoggerFactory.getLogger(TjpApplication.class);

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		String dbUrl = dotenv.get("DB_URL");
		String dbUsername = dotenv.get("DB_USERNAME");
		String dbPassword = dotenv.get("DB_PASSWORD");
		String googleClientId = dotenv.get("GOOGLE_CLIENT_ID");
		String googleClientSecret = dotenv.get("GOOGLE_CLIENT_SECRET");
		String jwtSecretKey = dotenv.get("JWT_SECRET_KEY");

		System.setProperty("DB_URL", dbUrl);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);

		System.setProperty("GOOGLE_CLIENT_ID", googleClientId);
		System.setProperty("GOOGLE_CLIENT_SECRET", googleClientSecret);

		System.setProperty("JWT_SECRET_KEY", jwtSecretKey);

		SpringApplication.run(TjpApplication.class, args);
		System.out.print("Hello tj!");
	}

}
