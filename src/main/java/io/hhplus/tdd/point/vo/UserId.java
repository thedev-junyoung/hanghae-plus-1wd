package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointErrorMessages;

public record UserId(long value) {
    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException(PointErrorMessages.USER_NEGATIVE_ID);
        }
    }
}