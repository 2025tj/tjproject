//package com.tj.tjp.common.config;
//
//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class OpenAPIConfig {
//
//    @Bean
//    public OpenAPI openAPI() {
//        // jwt 인증 스키마 이름(Authorize 버튼용)
//        final String securitySchemeName = "bearerAuth";
//
//        return new OpenAPI()
//                .info(new Info()
//                        .title("TJP API")
//                        .description("구독 서비스 Swagger 문서")
//                        .version("v1"))
//                .addSecurityItem(new SecurityRequirement()
//                        .addList(securitySchemeName))
//                .components(new Components()
//                        .addSecuritySchemes(securitySchemeName,
//                                new SecurityScheme()
//                                        .name(securitySchemeName)
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")));
//    }
//}
