package io.hhplus.tdd.policy;

public class PointPolicy {
    // ==== 보유 정책 ====
    // 유저별 최대 포인트 잔고
    public static final long MAX_POINT_BALANCE = 100_000_000L;

    // ==== 충전 정책 ====
    // 최소 충전 금액
    public static final long MIN_CHARGE_AMOUNT = 1_000L;
    // 최대 충전 금액
    public static final long MAX_CHARGE_AMOUNT = 1_000_000L;
    // 하루 충전 한도
    public static final long DAILY_CHARGE_LIMIT = 3_000_000L;

    // ====  사용 정책 ====
    // 1회 사용 최대 금액 (단일 거래 한도)
    public static final long MAX_USE_AMOUNT_PER_TRANSACTION = 10_000_000L;

    // 1일 누적 사용 최대 금액 (하루 한도)
    public static final long MAX_USE_AMOUNT_PER_DAY = 10_000_000L;

    // 최소 사용 금액
    public static final long MIN_USE_AMOUNT = 100L;


    private PointPolicy() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}
