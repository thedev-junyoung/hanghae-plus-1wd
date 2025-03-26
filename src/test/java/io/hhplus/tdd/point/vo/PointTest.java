package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Point VO 단위 테스트
 * 작성 이유:
 * - 포인트 충전/사용과 관련된 핵심 도메인 로직은 Point 객체 내부에 위임되어 있음
 * - 최대 잔액 초과, 잔액 부족, 잔액 비교 등 도메인 정책에 따른 예외 처리 로직이 포함되어 있어
 *   Point 객체 자체의 단위 테스트가 반드시 필요
 * 테스트 항목:
 * 1. 유효한 충전 요청에 대해 올바르게 포인트가 증가하는지 확인
 * 2. 충전 결과가 최대 잔액을 초과하면 예외가 발생하는지 검증
 * 3. 유효한 포인트 사용 요청에 대해 올바르게 포인트가 차감되는지 확인
 * 4. 잔액보다 많은 금액을 사용하려 할 경우 예외 발생 여부 검증
 * 5. 포인트가 충분한지 여부를 판별하는 isSufficient 메서드 검증
 * 6. add 메서드로 정상적으로 포인트를 더할 수 있는지 검증
 */
class PointTest {

    @Test
    void Point_충전_성공() {
        Point point = new Point(1000);
        ChargeAmount chargeAmount = new ChargeAmount(5000);

        Point newPoint = point.charge(chargeAmount);

        assertEquals(6000, newPoint.value());
    }

    @Test
    void Point_충전후_최대잔액초과_실패() {
        Point point = new Point(PointPolicy.MAX_POINT_BALANCE - 1000);
        ChargeAmount chargeAmount = new ChargeAmount(2000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> point.charge(chargeAmount)
        );

        assertEquals(DomainErrorMessages.MAX_CHARGE, exception.getMessage());
    }

    @Test
    void Point_사용_성공() {
        Point point = new Point(10000);
        UseAmount useAmount = new UseAmount(3000);

        Point newPoint = point.use(useAmount);

        assertEquals(7000, newPoint.value());
    }

    @Test
    void Point_잔액부족_사용실패() {
        Point point = new Point(1000);
        UseAmount useAmount = new UseAmount(2000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> point.use(useAmount)
        );

        assertEquals(DomainErrorMessages.INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void Point_잔액충분여부_확인_성공() {
        Point point = new Point(5000);
        UseAmount sufficientAmount = new UseAmount(3000);
        UseAmount insufficientAmount = new UseAmount(7000);
        assertTrue(point.isSufficient(sufficientAmount));
        assertFalse(point.isSufficient(insufficientAmount));
    }

    @Test
    void Point_add_메소드_성공() {
        Point point = new Point(3000);
        ChargeAmount chargeAmount = new ChargeAmount(2000);

        Point newPoint = point.add(chargeAmount);

        assertEquals(5000, newPoint.value());
    }
}