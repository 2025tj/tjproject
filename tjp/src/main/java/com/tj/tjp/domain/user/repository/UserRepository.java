package com.tj.tjp.domain.user.repository;

import com.tj.tjp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);

    @Query("select u from User u " +
            "where u.status = 'INACTIVE' and u.deleteScheduledAt is not null and u.deleteScheduledAt <= :now")
    List<User> findDueForHardDelete(@Param("now") LocalDateTime now);
}
