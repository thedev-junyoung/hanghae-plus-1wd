package io.hhplus.tdd.domain.point.vo;

import io.hhplus.tdd.domain.point.policy.PointPolicy;
import io.hhplus.tdd.domain.point.error.DomainErrorMessages;

/**
 * 포인트 충전 금액을 나타내는 값 객체
 * 충전 금액에 대한 비즈니스 규칙을 검증:
 * - 최소 충전 금액(MIN_CHARGE_AMOUNT) 이상이어야 함
 * - 최대 충전 금액(MAX_CHARGE_AMOUNT) 이하여야 함
 */
public class ChargeAmount {

    private final long value;

    // 외부에서 직접 생성하지 못하도록 생성자 private 처리
    private ChargeAmount(long value) {
        this.value = value;
    }

    // 정책에 대한 유효성 검증을 통과한 ChargeAmount 객체 생성
    public static ChargeAmount validated(long value) {
        if (value < PointPolicy.MIN_CHARGE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MIN_CHARGE);
        if (value > PointPolicy.MAX_CHARGE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MAX_CHARGE);
        return new ChargeAmount(value);
    }

    // 충전 금액 반환
    public long value() {
        return value;
    }
}