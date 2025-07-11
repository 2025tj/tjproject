package com.tj.tjp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());

        // getParameter()는 쿼리스트링/form 데이터만 읽음, 지금은 setAttribute()했으므로 getAttribute사용
        Object emailObj= request.getAttribute("email");
        Object providerObj = request.getAttribute("provider");

        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        String email= emailObj !=null ? URLEncoder.encode(emailObj.toString(), StandardCharsets.UTF_8) : ""; //있으면 추출
        String provider = providerObj !=null ? URLEncoder.encode(providerObj.toString(), StandardCharsets.UTF_8): ""; //있으면 추출
        String redirectUrl = "/oauth2/redirect?error=" +errorMessage;
        if (email != null && !email.isEmpty()) {
            redirectUrl += "&email="+email;
        }
        if (provider != null && !provider.isEmpty()) {
            redirectUrl += "&provider=" +provider;
        }
        response.sendRedirect(redirectUrl);

    }
}
