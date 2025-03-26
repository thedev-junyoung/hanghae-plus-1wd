package io.hhplus.tdd.point;

import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.policy.error.DomainErrorMessages;
import io.hhplus.tdd.policy.error.ServiceErrorMessages;
import io.hhplus.tdd.utils.ITimeProvider;
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

    @Mock
    private ITimeProvider timeProvider;

// ================== charge ==================
// charge - 성공 케이스
    // 정상 범위 충전
    @Test
    void charge_정상포인트_충전_성공() {
        // given
        long existing = 1_000_000L; // 잔액
        long amount = 500_000L; // 충전 포인트
        long expectedTotal = existing + amount; // 예상 잔액
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expected = new UserPoint(USER_ID, expectedTotal, now);

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));
        // 충전 로직에 대한 mock
        when(userPointTable.insertOrUpdate(USER_ID, expectedTotal)).thenReturn(expected);

        // when
        UserPoint result = pointService.charge(USER_ID, amount);

        // then
        assertEquals(expectedTotal, result.point());
    }
    // 최소 충전 포인트
    @Test
    void charge_최소포인트_충전_성공() {
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
    // 최대 충전 포인트
    @Test
    void charge_최대포인트_충전_성공() {
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
    void charge_하루한도_직전까지_충전_성공() {
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
    void charge_유효하지_사용자_ID_검증_실패(long invalidUserId) {
        long amount = 1_000L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.charge(invalidUserId, amount));
        assertEquals(DomainErrorMessages.USER_NEGATIVE_ID, exception.getMessage());
    }
    // 정책상 최소 충전 포인트 미만
    @Test
    void charge_하루한도_초과_충전_실패() {
        long amount = 1_000L;
        long fixedStart = 1_000_000_000L;
        long fixedNow = fixedStart + 1000;
        long fixedTomorrow = fixedStart + 24 * 60 * 60 * 1000;

        when(timeProvider.getStartOfTodayMillis()).thenReturn(fixedStart);
        when(timeProvider.getStartOfTomorrowMillis()).thenReturn(fixedTomorrow);
        when(timeProvider.getCurrentTimeMillis()).thenReturn(fixedNow);

        long now = timeProvider.getCurrentTimeMillis();

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 500_000L, now));

        List<PointHistory> fakeHistory = List.of(
                new PointHistory(1, USER_ID, 2_000_000L, TransactionType.CHARGE, fixedNow),
                new PointHistory(2, USER_ID, 1_000_000L, TransactionType.CHARGE, fixedNow)
        );
        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(fakeHistory);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.charge(USER_ID, amount)
        );
        assertEquals(ServiceErrorMessages.DAILY_CHARGE_LIMIT, exception.getMessage());
    }
// ================== charge 끝 ==================
// ================== use ==================
// use - 성공 케이스
    @Test
    void use_유효한_사용자_ID_유효한_포인트_사용_성공() {
        long amount = 500_000L;
        long existing = 1_000_000L;
        long expected = existing - amount;
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        // 기존 포인트 Stubbing
        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);

        // 사용자 포인트 조회 Stubbing
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));


        UserPoint result = pointService.use(USER_ID, amount);
        System.out.println(result);
        assertEquals(expected, result.point());
    }
    @Test
    void use_최소_포인트_사용_성공() {
        long amount = PointPolicy.MIN_USE_AMOUNT;
        long existing = 1_000_000L;
        long expected = existing - amount;
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));


        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    @Test
    void use_최대_포인트_사용_성공(){
        long amount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION; // 10_000_000
        long existing = 10_000_100L;
        long expected = existing - amount;
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        System.out.println(result.point());
        assertEquals(expected, result.point());
    }
    @Test
    void use_하루_사용_한도_미만_사용_성공() {
        long amount = 1_000_000L;
        long existing = 10_000_000L;
        long expected = existing - amount;
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    @Test
    void use_전체_포인트_정확히_모두_사용_성공(){
        long amount = 1_000_000L;
        long existing = 1_000_000L;
        long expected = 0;
        long now = timeProvider.getCurrentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    @Test
    void use_최소_사용_포인트_정상처리_성공() {
        long amount = PointPolicy.MIN_USE_AMOUNT; // 100L
        long existing = 10_000L;
        long expected = existing - amount;
        long now = timeProvider.getCurrentTimeMillis();

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));
        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(new UserPoint(USER_ID, expected, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
// --------------------------------------
// use - 실패 케이스
    @Test
    void use_보유_포인트_보다_많은_포인트_사용_실패() {
        long amount = 1_000_000L;
        long existing = 500_000L;

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.use(USER_ID, amount));
        assertEquals(DomainErrorMessages.INSUFFICIENT_BALANCE, exception.getMessage());
    }
    @Test
    void use_일일_사용_포인트_초과_실패() {
        // given
        long amountToUse = 1_000_000L;
        long now = System.currentTimeMillis();
        long startOfToday = now - 1000;
        long startOfTomorrow = now + 1000;

        // timeProvider mock
        when(timeProvider.getStartOfTodayMillis()).thenReturn(startOfToday);
        when(timeProvider.getStartOfTomorrowMillis()).thenReturn(startOfTomorrow);

        // 오늘 이미 사용한 금액: 9,500,000
        List<PointHistory> usageHistoryToday = List.of(
                new PointHistory(1, USER_ID, 9_500_000L, TransactionType.USE, now)
        );
        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(usageHistoryToday);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.use(USER_ID, amountToUse)
        );

        assertEquals(ServiceErrorMessages.MAX_USE_AMOUNT_PER_DAY, exception.getMessage());
    }
// ================== use 끝 ==================
// use - 실패 케이스
// ================== use 끝 ==================
}
