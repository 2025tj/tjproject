package com.tj.tjp.domain.auth.dto.signup;

import com.tj.tjp.domain.auth.validator.EmailUnique;
import com.tj.tjp.domain.auth.validator.NicknameUnique;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    @EmailUnique
    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자 포함 8~20자여야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String confirmPassword;

    @NicknameUnique
    @NotBlank(message = "닉네임을 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,20}$", message = "닉네임은 2~20자의 한글, 영문 또는 숫자여야 합니다.")
    private String nickname;

    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}

