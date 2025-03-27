package io.hhplus.tdd.domain.point.dto;

import io.hhplus.tdd.common.error.ApiErrorMessages;
import io.hhplus.tdd.common.utils.AssertUtil;

/**
 * 클라이언트로부터 전달받은 충전 요청 데이터
 * 서비스 계층 진입 전 기본 검증 수행 (양수 검증)
 * @param amount
 */
public record ChargeRequest(long amount) {
    public ChargeRequest {
        AssertUtil.requirePositive(amount, ApiErrorMessages.AMOUNT_NEGATIVE);
    }
}
