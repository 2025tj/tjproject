package com.tj.tjp.domain.auth.dto.signup;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OAuth2SignupRequest {

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "소셜 프로바이더를 지정하세요.")
    private String provider;       // e.g. "google", "kakao", "naver"

    @NotBlank(message = "프로바이더 고유 ID를 전달하세요.")
    private String providerId;     // e.g. Google의 sub, 카카오의 id

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자 포함 8~20자여야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String confirmPassword;

    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @NotBlank(message = "닉네임을 입력하세요.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]{2,20}$",
            message = "닉네임은 2~20자의 한글, 영문 또는 숫자여야 합니다."
    )
    private String nickname;
}
