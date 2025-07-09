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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "user_id")  // FK 컬럼명 명시
    )
    @Column(name = "role")  // 컬럼명 명시
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    public void update(String password) {
        this.password = password;
    }
}
