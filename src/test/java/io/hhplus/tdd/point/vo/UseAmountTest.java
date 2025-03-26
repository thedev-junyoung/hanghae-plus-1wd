package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UseAmount VO 단위 테스트
 * 작성 이유:
 * - 포인트 사용 시 사용 금액은 최소/최대 한도를 반드시 지켜야 하며,
 *   해당 정책 검증 로직은 UseAmount 객체 생성자에서 수행됨
 * - 서비스에서 이 값 객체를 사용하는 만큼, 값 생성 시 정책 위반을 검증하는 테스트가 필요함
 * 테스트 항목:
 * 1. 최소 사용 금액이 정상적으로 생성되는지 확인
 * 2. 최대 사용 금액이 정상적으로 생성되는지 확인
 * 3. 일반적인 유효 금액도 생성되는지 확인
 * 4. 최소 금액 미만 요청 시 예외 발생 여부 확인
 * 5. 최대 금액 초과 요청 시 예외 발생 여부 확인
 */

class UseAmountTest {

    @Test
    void UseAmount_최소금액_성공() {
        long minAmount = PointPolicy.MIN_USE_AMOUNT;

        UseAmount useAmount = new UseAmount(minAmount);

        assertEquals(minAmount, useAmount.value());
    }

    @Test
    void UseAmount_최대금액_성공() {
        long maxAmount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION;

        UseAmount useAmount = new UseAmount(maxAmount);

        assertEquals(maxAmount, useAmount.value());
    }

    @Test
    void UseAmount_일반금액_성공() {
        long validAmount = 5000;

        UseAmount useAmount = new UseAmount(validAmount);

        assertEquals(validAmount, useAmount.value());
    }

    @Test
    void UseAmount_최소금액미만_실패() {
        long invalidAmount = PointPolicy.MIN_USE_AMOUNT - 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UseAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MIN_USE, exception.getMessage());
    }

    @Test
    void UseAmount_최대금액초과_실패() {
        long invalidAmount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION + 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UseAmount(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MAX_USE, exception.getMessage());
    }
}