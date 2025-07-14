package com.tj.tjp.dto;

import com.tj.tjp.entity.user.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkSocialRequest {
    private String email;
    private ProviderType provider; // GOOGLE 등
}
