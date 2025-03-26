package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.error.DomainErrorMessages;

import static io.hhplus.tdd.policy.PointPolicy.*;

/**
 * 포인트 사용량을 나타내는 값 객체
 * 단일 거래에 대한 비즈니스 규칙을 검증:
 * - 최소 사용 금액(MIN_USE_AMOUNT) 이상이어야 함
 * - 단일 거래 최대 사용 금액(MAX_USE_AMOUNT_PER_TRANSACTION) 이하여야 함
 */
public record UseAmount(long value) {
    public UseAmount {  // compact constructor 사용
        if (value < MIN_USE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MIN_USE);
        if (value > MAX_USE_AMOUNT_PER_TRANSACTION)  // 단일 거래 최대 금액으로 변경
            throw new IllegalArgumentException(DomainErrorMessages.MAX_USE);
    }
}