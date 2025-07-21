package com.tj.tjp.domain.auth.security.service;

import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.repository.UserRepository;
import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user= userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UsernameNotFoundException("비밀번호가 설정되지 않은 계정입니다. 마이페이지에서 연동 후 사용하세요.");
        }

        return new LocalUserPrincipal(user); // password 포함한 생성자
    }
}
