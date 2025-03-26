package io.hhplus.tdd.policy.error;

public class ApiErrorMessages {
    public static final String AMOUNT_NEGATIVE = "포인트는 음수가 될 수 없습니다.";
    // API 계층에서 사용하는 기본 검증용 메시지들

    private ApiErrorMessages() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}