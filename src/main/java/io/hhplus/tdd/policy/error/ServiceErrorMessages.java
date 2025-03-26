package io.hhplus.tdd.policy.error;

public class ServiceErrorMessages {
    public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";
    public static final String DAILY_CHARGE_LIMIT = "하루 최대 충전 한도를 초과하였습니다.";
    public static final String MAX_USE_AMOUNT_PER_DAY = "하루 최대 사용 한도를 초과하였습니다.";

    private ServiceErrorMessages() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}