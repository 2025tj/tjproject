package com.tj.tjp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name="password", nullable = true)
    private String password;

    @Column(name="nickname", nullable=true, unique=true, length=30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name="provider", nullable=false, length=20)
    private ProviderType provider;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "user_id")  // FK 컬럼명 명시
    )
    @Column(name = "role")  // 컬럼명 명시
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    public void updateProvider(ProviderType provider) {
        this.provider=provider;
    }
}
