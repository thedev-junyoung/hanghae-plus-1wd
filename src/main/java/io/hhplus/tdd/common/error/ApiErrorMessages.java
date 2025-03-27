package io.hhplus.tdd.common.error;

// API 계층에서 사용하는 기본 검증용 메시지들
public class ApiErrorMessages {
    public static final String AMOUNT_NEGATIVE = "포인트는 음수가 될 수 없습니다.";
    public static final String USER_ID_NEGATIVE = "사용자 ID는 음수가 될 수 없습니다.";
    private ApiErrorMessages() {
        throw new AssertionError("인스턴스 생성 불가");
    }
}