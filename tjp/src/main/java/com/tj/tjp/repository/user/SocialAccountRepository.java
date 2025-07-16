package com.tj.tjp.repository.user;

import com.tj.tjp.entity.user.SocialAccount;
import com.tj.tjp.entity.user.User;
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

