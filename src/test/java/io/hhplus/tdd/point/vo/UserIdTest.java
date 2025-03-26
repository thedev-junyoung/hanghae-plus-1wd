package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    void UserId_양수_성공() {
        long validId = 1;
        UserId userId = new UserId(validId);
        assertEquals(validId, userId.value());
    }


    @Test
    void UserId_0_실패() {
        long invalidId = 0;
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UserId(invalidId)
        );
        assertEquals(DomainErrorMessages.USER_NEGATIVE_ID, exception.getMessage());
    }

    @Test
    void UserId_음수_실패() {
        long invalidId = -1;
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UserId(invalidId)
        );
        assertEquals(DomainErrorMessages.USER_NEGATIVE_ID, exception.getMessage());
    }
}