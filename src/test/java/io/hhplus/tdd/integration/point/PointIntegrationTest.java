package io.hhplus.tdd.integration.point;

import io.hhplus.tdd.api.controller.PointController;
import io.hhplus.tdd.domain.point.dto.ChargeRequest;
import io.hhplus.tdd.domain.point.dto.UsePointRequest;
import io.hhplus.tdd.domain.point.error.DomainErrorMessages;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import io.hhplus.tdd.domain.point.model.UserPoint;
import io.hhplus.tdd.infrastructure.database.PointHistoryTable;
import io.hhplus.tdd.infrastructure.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class PointIntegrationTest {

    @Autowired
    private PointController pointController;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    private long userId;

    @BeforeEach
    void reset() {
        // UUID로 생성하고 절대값을 취해 중복을 방지
        userId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        userPointTable.insertOrUpdate(userId, 0L);
    }

    /**
     * 포인트 충전 테스트
     * 포인트 충전 후, 충전된 값이 정확히 반영되는지 검증
     */
    @Test
    void 통합_01_포인트_충전() {
        ChargeRequest request = new ChargeRequest(10000L);
        UserPoint result = pointController.charge(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.point()).isEqualTo(10000L);

        // 상태 검증: 충전 후 포인트 테이블에 값이 제대로 저장됐는지 확인
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(10000L);

        // 이력 테이블 일관성 검증
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(0).amount()).isEqualTo(10000L);
    }

    /**
     * 포인트 사용 테스트
     * 충전된 포인트에서 일부를 사용하고, 잔액이 올바르게 차감되었는지 검증
     */
    @Test
    void 통합_02_포인트_사용() {
        userPointTable.insertOrUpdate(userId, 10000L);
        UsePointRequest request = new UsePointRequest(3000L);
        UserPoint result = pointController.use(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.point()).isEqualTo(7000L);

        // 상태 검증: 사용 후 포인트가 올바르게 차감되었는지 확인
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(7000L);

        // 이력 테이블 일관성 검증
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.USE);
        assertThat(histories.get(0).amount()).isEqualTo(3000L);
    }

    /**
     * 포인트 조회 테스트
     * 포인트 조회 후, 조회된 값이 정확히 반영되는지 검증
     */
    @Test
    void 통합_03_포인트_조회() {
        userPointTable.insertOrUpdate(userId, 12345L);
        UserPoint result = pointController.point(userId);

        assertThat(result).isNotNull();
        assertThat(result.point()).isEqualTo(12345L);
    }

    /**
     * 포인트 이력 조회 테스트
     * 포인트 충전 및 사용 후 이력이 정확히 조회되는지 검증
     */
    @Test
    void 통합_04_포인트_이력_조회() {
        pointController.charge(userId, new ChargeRequest(1000L));
        pointController.use(userId, new UsePointRequest(500L));

        List<PointHistory> histories = pointController.history(userId);
        assertThat(histories).hasSize(2);

        List<PointHistory> dbHistories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(dbHistories).hasSize(2);
        assertThat(dbHistories).isEqualTo(histories);

        // 내역 내용 검증
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(0).amount()).isEqualTo(1000L);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(histories.get(1).amount()).isEqualTo(500L);
    }

    /**
     * 잔액 부족 시 포인트 사용 실패 검증
     * 예외 처리가 제대로 동작하는지 검증
     */
    @Test
    void 통합_05_잔액_부족_사용_실패() {
        userPointTable.insertOrUpdate(userId, 1000L);
        UsePointRequest request = new UsePointRequest(2000L);

        assertThatThrownBy(() -> pointController.use(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DomainErrorMessages.INSUFFICIENT_BALANCE);

        // 예외 발생 후 잔액 확인
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(1000L);
    }

    /**
     * 트랜잭션 일관성 검증
     * 포인트 충전과 사용이 정상적으로 연동되고 일관성을 유지하는지 검증
     */
    @Test
    void 통합_06_트랜잭션_일관성_검증() {
        pointController.charge(userId, new ChargeRequest(5000L));
        assertThat(userPointTable.selectById(userId).point()).isEqualTo(5000L);

        pointController.use(userId, new UsePointRequest(3000L));
        assertThat(userPointTable.selectById(userId).point()).isEqualTo(2000L);

        pointController.charge(userId, new ChargeRequest(1000L));
        assertThat(userPointTable.selectById(userId).point()).isEqualTo(3000L);
    }
}
