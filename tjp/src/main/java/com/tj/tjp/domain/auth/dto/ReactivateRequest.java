package com.tj.tjp.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReactivateRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
