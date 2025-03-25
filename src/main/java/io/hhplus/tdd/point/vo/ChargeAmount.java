package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointErrorMessages;
import io.hhplus.tdd.policy.PointPolicy;

public record ChargeAmount(long value) {
    public ChargeAmount {
        if (value <= 0)
            throw new IllegalArgumentException(PointErrorMessages.AMOUNT_NEGATIVE);
        if (value < PointPolicy.MIN_CHARGE_AMOUNT)
            throw new IllegalArgumentException(PointErrorMessages.MIN_CHARGE);
        if (value > PointPolicy.MAX_CHARGE_AMOUNT)
            throw new IllegalArgumentException(PointErrorMessages.MAX_CHARGE);
    }
}