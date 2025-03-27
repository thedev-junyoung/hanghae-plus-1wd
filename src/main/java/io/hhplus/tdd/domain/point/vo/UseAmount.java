package io.hhplus.tdd.domain.point.vo;

import io.hhplus.tdd.domain.point.policy.PointPolicy;
import io.hhplus.tdd.domain.point.error.DomainErrorMessages;

/**
 * 포인트 사용량을 나타내는 값 객체
 * 단일 거래에 대한 비즈니스 규칙을 검증:
 * - 최소 사용 금액(MIN_USE_AMOUNT) 이상이어야 함
 * - 단일 거래 최대 사용 금액(MAX_USE_AMOUNT_PER_TRANSACTION) 이하여야 함
 */
public class UseAmount {

    private final long value;

    // 외부에서 직접 생성하지 못하도록 생성자 private 처리
    private UseAmount(long value) {
        this.value = value;
    }

    // 정책에 대한 유효성 검증을 통과한 UseAmount 객체 생성
    public static UseAmount validated(long value) {
        if (value < PointPolicy.MIN_USE_AMOUNT)
            throw new IllegalArgumentException(DomainErrorMessages.MIN_USE);
        if (value > PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION)
            throw new IllegalArgumentException(DomainErrorMessages.MAX_USE);
        return new UseAmount(value);
    }

    // 사용 금액 반환
    public long value() {
        return value;
    }
}
