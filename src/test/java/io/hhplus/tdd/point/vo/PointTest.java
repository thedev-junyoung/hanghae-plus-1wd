package io.hhplus.tdd.point.vo;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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