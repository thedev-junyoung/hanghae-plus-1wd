package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.error.DomainErrorMessages;


public record UserId(long value) {
    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException(DomainErrorMessages.USER_NEGATIVE_ID);
        }
    }
}