package com.tj.tjp.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationErrorResponse {
    private String status;
    private String message;
    private String email;
    private boolean canResend;
    private LocalDateTime timestamp;

    public static EmailVerificationErrorResponse unverified(String email, boolean canResend) {
        return EmailVerificationErrorResponse.builder()
                .status("UNVERIFIED_EMAIL")
                .message("이메일 인증이 필요합니다.")
                .email(email)
                .canResend(canResend)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
