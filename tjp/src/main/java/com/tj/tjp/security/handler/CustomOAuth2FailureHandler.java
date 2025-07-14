//package com.tj.tjp.security;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//@Slf4j
//@Component
//public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException, ServletException {
//        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());
//
//        String rawMessage = exception.getMessage();
//        String errorMessage = URLEncoder.encode(rawMessage, StandardCharsets.UTF_8);
//
//        //이메일과 provider추출(정규표현식)
//        String email=null;
//        String provider=null;
//        try {
//            var emailMatcher = java.util.regex.Pattern.compile("\\[email=(.*?)]").matcher(rawMessage);
//            if (emailMatcher.find()) email = URLEncoder.encode(emailMatcher.group(1), StandardCharsets.UTF_8);
//
//            var providerMatcher = java.util.regex.Pattern.compile("\\[provider=(.*?)]").matcher(rawMessage);
//            if (providerMatcher.find()) provider = URLEncoder.encode(providerMatcher.group(1), StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            log.warn("OAuth2 로그인 에러 메세지 파싱 실패: {}", e.getMessage());
//        }
//
//        String redirectUrl = "/oauth2/redirect?error="+errorMessage;
//        if (email !=null) redirectUrl += "&email="+email;
//        if (provider !=null) redirectUrl += "&provider="+provider;
//
//        response.sendRedirect(redirectUrl);
//    }
//}
