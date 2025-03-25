package io.hhplus.tdd.policy;

public class PointErrorMessages {



    public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";
    public static final String USER_NEGATIVE_ID = "사용자 ID는 음수가 될 수 없습니다.";
    public static final String AMOUNT_NEGATIVE = "포인트는 음수가 될 수 없습니다.";
    public static final String MIN_CHARGE = "최소 충전 금액을 충족하지 못했습니다.";
    public static final String MAX_CHARGE = "최대 충전 금액을 초과하였습니다.";
    public static final String DAILY_CHARGE_LIMIT = "하루 최대 충전 한도를 초과하였습니다.";
    public static final String INSUFFICIENT_BALANCE = "잔액이 부족합니다.";
    public static final String MIN_USE = "최소 사용 금액을 충족하지 못했습니다.";
    public static final String MAX_USE = "최대 사용 금액을 초과하였습니다.";


    public static final String MAX_USE_AMOUNT_PER_DAY = "하루 최대 사용 한도를 초과하였습니다.";

    private PointErrorMessages() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}
