package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;

public record Point(long value) {
    public Point add(ChargeAmount amount) {
        return new Point(this.value + amount.value());
    }

    public Point use(UseAmount amount) {
        if (this.value < amount.value()) {
            throw new IllegalArgumentException(DomainErrorMessages.INSUFFICIENT_BALANCE);
        }
        return new Point(this.value - amount.value());
    }

    public boolean isSufficient(UseAmount amount) {
        return this.value >= amount.value();
    }

    public Point charge(ChargeAmount amount) {
        // 최대 잔액 초과 검증
        if (this.value + amount.value() > PointPolicy.MAX_POINT_BALANCE) {
            throw new IllegalArgumentException(DomainErrorMessages.MAX_CHARGE);
        }
        return this.add(amount);
    }

}

