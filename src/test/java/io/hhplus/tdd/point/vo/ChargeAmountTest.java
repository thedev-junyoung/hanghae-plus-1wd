package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.domain.point.policy.PointPolicy;
import io.hhplus.tdd.domain.point.error.DomainErrorMessages;
import io.hhplus.tdd.domain.point.vo.ChargeAmount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * ChargeAmount VO 단위 테스트
 * 작성 이유:
 * - 충전 금액에 대한 정책(PointPolicy)을 객체 생성 시점에 강제하기 위해 VO로 분리하였음
 * - 도메인 유효성 검증이 VO에 위임되었기 때문에 해당 클래스에 대한 단위 테스트가 반드시 필요
 * 테스트 항목:
 * 1. 최소/최대 금액 충전이 정상적으로 생성되는지 확인 (정책 경계값 검증)
 * 2. 일반적인 금액도 정상 생성되는지 확인
 * 3. 최소 금액 미만, 최대 금액 초과 시 예외가 발생하는지 확인 (정책 위반 케이스)
 */
class ChargeAmountTest {

    // [정책] 최소 금액, 최대 금액, 일반 금액 테스트
    @Test
    void ChargeAmount_최소금액_성공() {
        long minAmount = PointPolicy.MIN_CHARGE_AMOUNT;
        ChargeAmount chargeAmount = ChargeAmount.validated(minAmount);
        assertEquals(minAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_최대금액_성공() {
        long maxAmount = PointPolicy.MAX_CHARGE_AMOUNT;
        ChargeAmount chargeAmount = ChargeAmount.validated(maxAmount);
        assertEquals(maxAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_일반금액_성공() {
        long validAmount = 50_000L;
        ChargeAmount chargeAmount = ChargeAmount.validated(validAmount);
        assertEquals(validAmount, chargeAmount.value());
    }

    @Test
    void ChargeAmount_음수금액_실패() {
        long invalid = -500;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ChargeAmount.validated(invalid)
        );

        assertEquals(DomainErrorMessages.MIN_CHARGE, exception.getMessage());
    }

    @Test
    void ChargeAmount_최소금액미만_실패() {
        long invalidAmount = PointPolicy.MIN_CHARGE_AMOUNT - 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ChargeAmount.validated(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MIN_CHARGE, exception.getMessage());
    }

    @Test
    void ChargeAmount_최대금액초과_실패() {
        long invalidAmount = PointPolicy.MAX_CHARGE_AMOUNT + 1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ChargeAmount.validated(invalidAmount)
        );

        assertEquals(DomainErrorMessages.MAX_CHARGE, exception.getMessage());
    }
}