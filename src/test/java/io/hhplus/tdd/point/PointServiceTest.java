package io.hhplus.tdd.point;

import io.hhplus.tdd.policy.PointErrorMessages;
import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private static final long USER_ID = 1L;

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        userPoint = UserPoint.empty(USER_ID);
    }

// ================== charge ==================
// charge - 성공 케이스
    // 정상 범위 충전
    @Test
    void charge_유효한사용자_ID_정상_범위_충전() {
        // given
        long amount = 500_000L; // 충전 금액
        long existing = 1_000_000L; // 잔액
        long expectedTotal = existing + amount; // 예상 잔액

        UserPoint expected = new UserPoint(USER_ID, expectedTotal, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));


        // 잔액 조회에 대한 mock 추가
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        // 충전 로직에 대한 mock
        when(userPointTable.insertOrUpdate(USER_ID, expectedTotal)).thenReturn(expected);

        // when
        UserPoint result = pointService.charge(USER_ID, amount);

        // then
        assertEquals(expectedTotal, result.point());
    }

    // 최소 충전 금액
    @Test
    void charge_유효한사용자_ID_최소_범위_충전() {
        long amount = PointPolicy.MIN_CHARGE_AMOUNT;
        long existing = 1_000_000L;
        long expected = existing + amount;
        UserPoint charged = new UserPoint(USER_ID, expected, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(charged);

        UserPoint result = pointService.charge(USER_ID, amount);
        assertEquals(expected, result.point());
    }

    // 최대 충전 금액
    @Test
    void charge_유효한사용자_ID_최대_범위_충전() {
        long amount = PointPolicy.MAX_CHARGE_AMOUNT;
        long existing = 1_000_000L;
        long expected = existing + amount;

        UserPoint charged = new UserPoint(USER_ID, expected, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        when(userPointTable.insertOrUpdate(USER_ID, expected)).thenReturn(charged);

        UserPoint result = pointService.charge(USER_ID, amount);
        assertEquals(expected, result.point());
    }

    // 하루 충전 한도 직전까지 충전 ( 추가된 정책 )
    @Test
    void charge_하루_충전한도_직전까지_충전() {
        long todayTotal = 2_999_000L;
        long amount = 1_000L;
        long existing = 500_000L;
        long expected = existing + amount;

        UserPoint charged = new UserPoint(USER_ID, expected, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        when(userPointTable.insertOrUpdate(USER_ID, expected)).thenReturn(charged);

        when(pointHistoryTable.insert(eq(USER_ID), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(1L, USER_ID, amount, TransactionType.CHARGE, System.currentTimeMillis()));

        UserPoint result = pointService.charge(USER_ID, amount);

        assertEquals(expected, result.point());
    }
//-------------------------------------------------
// charge - 실패 케이스
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void charge_유효하지_사용자_ID_검증(long invalidUserId) {
        long amount = 1_000L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.charge(invalidUserId, amount));
        assertEquals(PointErrorMessages.USER_NEGATIVE_ID, exception.getMessage());
    }

    // 정책상 최소 충전 금액 미만
    @Test
    void charge_유효한사용자_ID_최소_미만_충전() {
        long amount = PointPolicy.MIN_CHARGE_AMOUNT - 1;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.charge(USER_ID, amount));
        assertEquals(PointErrorMessages.MIN_CHARGE, exception.getMessage());
    }

    // 음수 충전 금액
    @Test
    void charge_유효한사용자_ID_음수_충전() {
        long amount = -1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.charge(USER_ID, amount));
        assertEquals(PointErrorMessages.AMOUNT_NEGATIVE, exception.getMessage());
    }

    @Test
    void charge_하루_최대_충전_한도_초과() {
        long amount = 1_000L;
        long now = System.currentTimeMillis();

        // 기존 포인트 mock (어차피 예외 나니까 얼마든 상관 X)
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 500_000L, now));

        List<PointHistory> fakeHistory = List.of(
                new PointHistory(1, USER_ID, 2_000_000L, TransactionType.CHARGE, now),
                new PointHistory(2, USER_ID, 1_000_000L, TransactionType.CHARGE, now)
        );
        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(fakeHistory);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.charge(USER_ID, amount)
        );
        assertEquals(PointErrorMessages.DAILY_CHARGE_LIMIT, exception.getMessage());
    }

// ================== charge 끝 ==================

// ================== use ==================
    @Test
    void use() {
    }


}
