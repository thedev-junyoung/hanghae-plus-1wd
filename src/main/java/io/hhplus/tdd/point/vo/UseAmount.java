package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointErrorMessages;

import static io.hhplus.tdd.policy.PointPolicy.*;

public record UseAmount(long value) {
    public UseAmount(long value) {
        if (value < MIN_USE_AMOUNT)
            throw new IllegalArgumentException(PointErrorMessages.MIN_USE);
        if (value > MAX_USE_AMOUNT_PER_DAY)
            throw new IllegalArgumentException(PointErrorMessages.MAX_USE);
        this.value = value;
    }
}
