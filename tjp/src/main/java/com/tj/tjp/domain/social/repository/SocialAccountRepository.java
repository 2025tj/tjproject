package com.tj.tjp.domain.social.repository;

import com.tj.tjp.domain.social.entity.SocialAccount;
import com.tj.tjp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(String provider, String providerId);

    // 연동 해제용
    long deleteByUserAndProvider(User user, String provider);

    List<SocialAccount> findByUser(User user);

}

