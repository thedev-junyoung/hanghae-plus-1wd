package io.hhplus.tdd.point.dto;


import io.hhplus.tdd.policy.error.ApiErrorMessages;

/**
 * 클라이언트로부터 전달받은 포인트 사용 요청 데이터
 * 서비스 계층 진입 전 기본 검증 수행 (양수 검증)
 * @param amount
 */
public record UsePointRequest(
        long amount
)
{
    public UsePointRequest {
        if (amount <= 0) {
            throw new IllegalArgumentException(ApiErrorMessages.AMOUNT_NEGATIVE);
        }
    }
}

