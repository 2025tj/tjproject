package com.tj.tjp.security;

import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User;
        try {
            log.warn("[OAuth2UserService] loadUser 시작");
            oAuth2User = super.loadUser(request);

            log.info("Attributes: {}", oAuth2User.getAttributes());

            String email = (String) oAuth2User.getAttributes().get("email");
            if (email == null) {
                throw new OAuth2AuthenticationException("이메일이 존재하지 않음");
            }

            // db에 사용자 없으면 생성
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .email(email)
                                    .roles(Set.of("ROLE_USER"))
                                    .build()
                    ));

//        String email= oAuth2User.getAttributes("email");
            List<String> roles = List.copyOf(user.getRoles()); // 기본권한?



            UserPrincipal userPrincipal = new UserPrincipal(email, roles);
            userPrincipal.setAttributes(oAuth2User.getAttributes());
//        return UserPrincipal.create(email, oAuth2User.getAttributes());
            return userPrincipal;
        } catch (Exception e) {
            log.error("[OAuth2UserService] 사용자 정보 로딩 실패 {}", e);
            throw new OAuth2AuthenticationException("사용자 정보 로딩중 예외");
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ CustomOAuth2UserService 빈 등록 완료");
    }

}
