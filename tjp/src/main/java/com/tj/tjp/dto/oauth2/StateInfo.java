package com.tj.tjp.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateInfo {
    private String mode;           // "signup", "link"
    private String token;          // 일회용 토큰
    private String originalState;  // 원본 OAuth2 state
    private Long timestamp;        // 생성 시간

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("StateInfo JSON 변환 실패", e);
        }
    }

    public static StateInfo fromJson(String json) {
        try {
            return objectMapper.readValue(json, StateInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("StateInfo JSON 파싱 실패", e);
        }
    }

    // 유효성 검증 (5분 이내 생성된 것만 유효)
    public boolean isValid() {
        if (timestamp == null) return false;
        long fiveMinutes = 5 * 60 * 1000;
        return System.currentTimeMillis() - timestamp < fiveMinutes;
    }
}