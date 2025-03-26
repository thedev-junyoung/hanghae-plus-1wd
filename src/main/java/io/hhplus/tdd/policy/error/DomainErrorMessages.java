package io.hhplus.tdd.policy.error;

public class DomainErrorMessages {
    public static final String USER_NEGATIVE_ID = "사용자 ID는 음수가 될 수 없습니다.";
    public static final String MIN_CHARGE = "최소 충전 금액을 충족하지 못했습니다.";
    public static final String MAX_CHARGE = "최대 충전 금액을 초과하였습니다.";
    public static final String MIN_USE = "최소 사용 금액을 충족하지 못했습니다.";
    public static final String MAX_USE = "최대 사용 금액을 초과하였습니다.";
    public static final String INSUFFICIENT_BALANCE = "잔액이 부족합니다.";

    private DomainErrorMessages() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}