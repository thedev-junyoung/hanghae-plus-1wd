package io.hhplus.tdd.domain.point.vo;

import io.hhplus.tdd.domain.point.policy.PointPolicy;
import io.hhplus.tdd.domain.point.error.DomainErrorMessages;

/**
 * 사용자의 포인트 잔액을 나타내는 값 객체 (Value Object)
 * - 포인트의 상태(금액)를 불변 객체로 캡슐화함
 * - 포인트 충전 및 사용 로직은 도메인 내부에서 책임지고 검증함
 *   (잔액 부족, 최대 충전 한도 초과 등)
 * - 해당 도메인 로직은 Service 레이어에서 위임하여 사용함
 */
public class Point {

    private final long value;

    // 외부에서 직접 생성하지 못하도록 생성자 private 처리
    private Point(long value) {
        this.value = value;
    }

    // 포인트 생성 (최소 유효성만 검증)
    public static Point of(long value) {
        if (value < 0) {
            throw new IllegalArgumentException(DomainErrorMessages.MIN_POINT);
        }
        return new Point(value);
    }

    // 포인트를 충전 금액만큼 더한 새 Point 객체 반환
    public Point add(ChargeAmount amount) {
        return new Point(this.value + amount.value());
    }

    // 포인트를 사용 금액만큼 차감한 새 Point 객체 반환
    public Point use(UseAmount amount) {
        if (this.value < amount.value()) {
            throw new IllegalArgumentException(DomainErrorMessages.INSUFFICIENT_BALANCE);
        }
        return new Point(this.value - amount.value());
    }

    // 포인트가 사용 금액보다 충분한지 여부 반환
    // 현재 서비스 구현에서는 UseAmount 객체를 통해 use() 메소드 내부에서
    // 직접 잔액 검증과 예외 처리를 수행하고 있어 이 메소드를 사용하지 않지만,
    // 포인트 잔액 충분성 검증은 Point 도메인의 핵심 책임이므로 명시적으로 유지
    public boolean isSufficient(UseAmount amount) {
        return this.value >= amount.value();
    }

    // 포인트 충전 시, 최대 잔액을 초과하지 않는지 검증하고
    // 초과하지 않으면 충전된 새 Point 객체 반환
    public Point charge(ChargeAmount amount) {
        // 최대 잔액 초과 검증
        if (this.value + amount.value() > PointPolicy.MAX_POINT_BALANCE) {
            throw new IllegalArgumentException(DomainErrorMessages.MAX_CHARGE);
        }
        return this.add(amount);
    }

    // 포인트 잔액 반환
    public long value() {
        return value;
    }

}

