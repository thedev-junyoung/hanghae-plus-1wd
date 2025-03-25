package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointErrorMessages;

public record UseAmount(long value) {
    public UseAmount {
        if (value <= 0) throw new IllegalArgumentException(PointErrorMessages.AMOUNT_NEGATIVE);
    }
}
