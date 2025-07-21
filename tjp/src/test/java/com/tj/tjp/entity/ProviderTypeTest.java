package com.tj.tjp.entity;

import com.tj.tjp.domain.social.entity.ProviderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderTypeTest {

    @Test
    void testFrom_validInputs() {
        assertEquals(ProviderType.GOOGLE, ProviderType.from("google"));
        assertEquals(ProviderType.LOCAL, ProviderType.from("local"));
    }

    @Test
    void testFrom_invalidInput() {
        assertThrows(RuntimeException.class, () -> ProviderType.from("test"));
    }
}