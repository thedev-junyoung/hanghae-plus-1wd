package io.hhplus.tdd.policy;

public class PointPolicy {
    // 포인트는 음수 불가능 (로직에서 assert or validation으로 처리)

    // 유저별 최대 포인트 잔고
    public static final long MAX_BALANCE = 100_000_000L;

    // 최소 충전 금액
    public static final long MIN_CHARGE_AMOUNT = 1_000L;

    // 최대 충전 금액
    public static final long MAX_CHARGE_AMOUNT = 1_000_000L;

    // 하루 충전 한도
    public static final long DAILY_CHARGE_LIMIT = 3_000_000L;

    // 하루 사용 한도 (추후 사용 정책용)
    public static final long DAILY_USE_LIMIT = 500_000L;

    private PointPolicy() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}
