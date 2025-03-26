package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargeAmountTest {

    @Test
    void ChargeAmount_최소금액_성공() {
        long minAmount = PointPolicy.MIN_CHARGE_AMOUNT;
        ChargeAmount chargeAmount = new ChargeAmount(minAmount);
        assertEquals(minAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_최대금액_성공() {
        long maxAmount = PointPolicy.MAX_CHARGE_AMOUNT;
        ChargeAmount chargeAmount = new ChargeAmount(maxAmount);
        assertEquals(maxAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_일반금액_성공() {
        long validAmount = 50_000L;
        ChargeAmount chargeAmount = new ChargeAmount(validAmount);
        assertEquals(validAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_최소금액미만_실패() {
        long invalidAmount = PointPolicy.MIN_CHARGE_AMOUNT - 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ChargeAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MIN_CHARGE, exception.getMessage());
    }

    @Test
    void ChargeAmount_최대금액초과_실패() {
        long invalidAmount = PointPolicy.MAX_CHARGE_AMOUNT + 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ChargeAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MAX_CHARGE, exception.getMessage());
    }
}