package com.tj.tjp.dto.auth.singup;

import com.tj.tjp.entity.user.ProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

    @NotBlank(message="이메일을 입력하세요.")
    @Email(message="올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message="비밀번호를 입력하세요.")
    @Size(min=6, message="비밀번호는 최소 6자 이상이어야합니다.")
    String password,

    @NotBlank(message="닉네임을 입력하세요.")
    String nickname
) {
    public ProviderType provider() {
        return ProviderType.LOCAL;
    }
}
