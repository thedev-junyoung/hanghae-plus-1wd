package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseAmountTest {

    @Test
    void UseAmount_최소금액_성공() {
        long minAmount = PointPolicy.MIN_USE_AMOUNT;

        UseAmount useAmount = new UseAmount(minAmount);

        assertEquals(minAmount, useAmount.value());
    }

    @Test
    void UseAmount_최대금액_성공() {
        long maxAmount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION;

        UseAmount useAmount = new UseAmount(maxAmount);

        assertEquals(maxAmount, useAmount.value());
    }

    @Test
    void UseAmount_일반금액_성공() {
        long validAmount = 5000;

        UseAmount useAmount = new UseAmount(validAmount);

        assertEquals(validAmount, useAmount.value());
    }

    @Test
    void UseAmount_최소금액미만_실패() {
        long invalidAmount = PointPolicy.MIN_USE_AMOUNT - 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UseAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MIN_USE, exception.getMessage());
    }

    @Test
    void UseAmount_최대금액초과_실패() {
        long invalidAmount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION + 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UseAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MAX_USE, exception.getMessage());
    }
}