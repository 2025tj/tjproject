package com.tj.tjp.dto;

import com.tj.tjp.validation.OptionalNickname;
import com.tj.tjp.validation.OptionalPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @OptionalNickname
    private String nickname;
    @OptionalPassword // 입력됐을 경우만 검사됨
    private String password;
}
