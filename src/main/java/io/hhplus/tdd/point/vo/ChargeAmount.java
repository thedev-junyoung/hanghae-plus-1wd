package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;

/**
 * 포인트 충전 금액을 나타내는 값 객체
 * 충전 금액에 대한 비즈니스 규칙을 검증:
 * - 최소 충전 금액(MIN_CHARGE_AMOUNT) 이상이어야 함
 * - 최대 충전 금액(MAX_CHARGE_AMOUNT) 이하여야 함
 */
public record ChargeAmount(long value) {
    public ChargeAmount {
        // ** 양수 검증은 DTO에서 이미 수행됨 **

        // 비즈니스 규칙 검증
        if (value < PointPolicy.MIN_CHARGE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MIN_CHARGE);
        if (value > PointPolicy.MAX_CHARGE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MAX_CHARGE);
    }
}