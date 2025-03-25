package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointErrorMessages;

public record Point(long value) {
    public Point add(ChargeAmount amount) {
        return new Point(this.value + amount.value());
    }

    public Point subtract(UseAmount amount) {
        if (this.value < amount.value()) {
            throw new IllegalArgumentException(PointErrorMessages.INSUFFICIENT_BALANCE);
        }
        return new Point(this.value - amount.value());
    }
}

