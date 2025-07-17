package com.tj.tjp;

import com.tj.tjp.config.properties.FrontendProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableConfigurationProperties(FrontendProperties.class)
@SpringBootApplication
public class TjpApplication {
	private static final Logger log = LoggerFactory.getLogger(TjpApplication.class);

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		//DataBase
		String dbUrl = dotenv.get("DB_URL");
		String dbUsername = dotenv.get("DB_USERNAME");
		String dbPassword = dotenv.get("DB_PASSWORD");

		//OAuth2
		String googleClientId = dotenv.get("GOOGLE_CLIENT_ID");
		String googleClientSecret = dotenv.get("GOOGLE_CLIENT_SECRET");
		String oauth2StateSecretKey = dotenv.get("OAUTH2_STATE_SECRET_KEY");

		//Jwt
		String jwtSecretKey = dotenv.get("JWT_SECRET_KEY");
		String oneTimeJwtSecretKey = dotenv.get("ONE_TIME_JWT_SECRET_KEY");

		//Frontend
		String frontendBaseUrl = dotenv.get("FRONTEND_BASE_URL");

		//Mail
		String mailUsername = dotenv.get("MAIL_USERNAME");
		String mailPassword = dotenv.get("MAIL_PASSWORD");

		System.setProperty("DB_URL", dbUrl);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);
		System.setProperty("GOOGLE_CLIENT_ID", googleClientId);
		System.setProperty("GOOGLE_CLIENT_SECRET", googleClientSecret);
		System.setProperty("JWT_SECRET_KEY", jwtSecretKey);
		System.setProperty("FRONTEND_BASE_URL", frontendBaseUrl);
		System.setProperty("MAIL_USERNAME", mailUsername);
		System.setProperty("MAIL_PASSWORD", mailPassword);
		System.setProperty("ONE_TIME_JWT_SECRET_KEY", oneTimeJwtSecretKey);
		System.setProperty("OAUTH2_STATE_SECRET_KEY", oauth2StateSecretKey);

		SpringApplication.run(TjpApplication.class, args);
		System.out.print("Hello tj!");
	}

}
