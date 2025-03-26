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


/**
 * PointServiceTest
 * 포인트 서비스의 주요 기능(충전, 사용, 조회, 이력)에 대한 단위 테스트 클래스.
 * 각 기능의 정상 동작 여부 및 정책 위반 케이스에서의 예외 처리를 검증한다.
 * 테스트 목적
 * - 비즈니스 로직이 정책에 따라 정확하게 동작하는지 확인
 * - 정책 경계값(min/max) 및 예외 상황을 명확히 테스트
 * - 외부 의존 객체(UserPointTable, PointHistoryTable, TimeProvider)를 mock 처리하여
 *   서비스 레이어 단위의 독립적인 테스트 보장
 * 테스트 범위
 * 1. charge(): 포인트 충전 (정상 / 최소 / 최대 / 한도 초과)
 * 2. use(): 포인트 사용 (정상 / 최소 / 최대 / 잔액 부족 / 일일 한도 초과)
 * 3. point(): 사용자 포인트 조회 (존재 / 0원 / 존재하지 않음)
 * 4. history(): 포인트 이력 조회 (존재 / 없음 / 존재하지 않음)
 */

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

    // [정상 케이스] 사용자가 유효하고, 하루 충전 한도 내에서 포인트 충전이 정상적으로 처리되는지 검증
    @Test
    void charge_정상포인트_충전_성공() {
        // given
        long existing = 1_000_000L; // 잔액
        long amount = 500_000L; // 충전 포인트
        long expectedTotal = existing + amount; // 예상 잔액
        long now = System.currentTimeMillis();

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
    // [경계값 테스트] 최소 충전 금액(정책 상 하한값)을 충전할 수 있는지 검증
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
    // [경계값 테스트] 최대 충전 금액(정책 상 상한값)을 충전할 수 있는지 검증
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
    // [정책 검증] 하루 충전 한도 직전까지는 충전이 가능한지, 정책 로직이 정확한지 확인
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
    // [정책 위반 테스트] 하루 충전 한도를 초과할 경우 예외가 발생하는지 검증
    @Test
    void charge_하루한도_초과_충전_실패() {
        long amount = 1_000L;
        long fixedStart = 1_000_000_000L;
        long fixedNow = fixedStart + 1000;
        long fixedTomorrow = fixedStart + 24 * 60 * 60 * 1000;

        when(timeProvider.getStartOfTodayMillis()).thenReturn(fixedStart);
        when(timeProvider.getStartOfTomorrowMillis()).thenReturn(fixedTomorrow);

        long now = System.currentTimeMillis();

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
    // [정상 케이스] 사용자의 보유 포인트 내에서 정상적으로 포인트를 사용할 수 있는지 검증
    @Test
    void use_유효한_포인트_사용_성공() {
        long amount = 500_000L;
        long existing = 1_000_000L;
        long expected = existing - amount;
        long now = System.currentTimeMillis();

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
    // [경계값 테스트] 최소 사용 금액으로도 포인트 사용이 가능한지 검증
    @Test
    void use_최소_포인트_사용_성공() {
        long amount = PointPolicy.MIN_USE_AMOUNT;
        long existing = 1_000_000L;
        long expected = existing - amount;
        long now = System.currentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));


        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    // [경계값 테스트] 최대 사용 금액까지 허용되는지 검증
    @Test
    void use_최대_포인트_사용_성공(){
        long amount = PointPolicy.MAX_USE_AMOUNT_PER_TRANSACTION; // 10_000_000
        long existing = 10_000_100L;
        long expected = existing - amount;
        long now = System.currentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        System.out.println(result.point());
        assertEquals(expected, result.point());
    }
    // [정책 검증] 하루 사용 한도 직전까지 사용이 가능한지 확인
    @Test
    void use_하루_사용_한도_미만_사용_성공() {
        long amount = 1_000_000L;
        long existing = 10_000_000L;
        long expected = existing - amount;
        long now = System.currentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    // [정책 검증] 사용자의 보유 포인트가 0일 때 정상적으로 포인트 사용이 가능한지 확인
    @Test
    void use_전체_포인트_정확히_모두_사용_성공(){
        long amount = 1_000_000L;
        long existing = 1_000_000L;
        long expected = 0;
        long now = System.currentTimeMillis();

        UserPoint expectedPoint = new UserPoint(USER_ID, expected, now);

        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(expectedPoint);
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
    // [정책 검증] 최소 사용 금액으로도 포인트 사용이 가능한지 확인
    @Test
    void use_최소_사용_포인트_정상_처리_성공() {
        long amount = PointPolicy.MIN_USE_AMOUNT; // 100L
        long existing = 10_000L;
        long expected = existing - amount;
        long now = System.currentTimeMillis();

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));
        when(userPointTable.insertOrUpdate(USER_ID, expected))
                .thenReturn(new UserPoint(USER_ID, expected, now));

        UserPoint result = pointService.use(USER_ID, amount);
        assertEquals(expected, result.point());
    }
// --------------------------------------
// use - 실패 케이스
    // [에러 케이스] 보유 포인트보다 많은 금액을 사용하려고 할 때 예외가 발생하는지 검증
    @Test
    void use_보유_포인트_보다_많은_포인트_사용_실패() {
        long amount = 1_000_000L;
        long existing = 500_000L;

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, System.currentTimeMillis()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.use(USER_ID, amount));
        assertEquals(DomainErrorMessages.INSUFFICIENT_BALANCE, exception.getMessage());
    }
    // [정책 위반 테스트] 하루 사용 한도를 초과할 경우 예외가 발생하는지 검증
    @Test
    void use_일일_사용_포인트_초과_실패() {
        // given
        long amountToUse = 1_000_000L;
        long userBalance = 10_000_000L; // 충분한 잔액 설정
        long now = System.currentTimeMillis();
        long startOfToday = now - 1000;
        long startOfTomorrow = now + 1000;

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, userBalance, now));


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

// ================== point ===================
// point 성공 TC
    // [정상 케이스] 사용자의 포인트 조회가 정상적으로 처리되는지 검증
    @Test
    void point_포인트_조회_성공() {
        long existing = 1_000_000L;
        long now = System.currentTimeMillis();

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.point(USER_ID);
        assertEquals(existing, result.point());
    }
    // [정상 케이스] 사용자의 포인트가 0일 때 조회가 정상적으로 처리되는지 검증
    @Test
    void point_포인트_0_조회_성공() {
        long existing = 0L;
        long now = System.currentTimeMillis();

        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, existing, now));

        UserPoint result = pointService.point(USER_ID);
        assertEquals(existing, result.point());
    }
// point 실패 TC
    // [에러 케이스] 존재하지 않는 사용자의 포인트 조회 시 예외가 발생하는지 검증
    @Test
    void point_존재하지_않는_사용자_조회_실패() {
        when(userPointTable.selectById(USER_ID)).thenReturn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.point(USER_ID));
        assertEquals(ServiceErrorMessages.USER_NOT_FOUND, exception.getMessage());
    }

//  ========== point 끝 =======================

//  ========== history =======================
//  history - 성공 케이스
    // [정상 케이스] 사용자의 포인트 이력 조회가 정상적으로 처리되는지 검증
    @Test
    void history_포인트_이력_조회_성공() {
        long now = System.currentTimeMillis();
        // 유저 존재 mock 추가
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 1000L, now));

        List<PointHistory> mockHistory = List.of(
                new PointHistory(1, USER_ID, 1000L, TransactionType.CHARGE, now),
                new PointHistory(2, USER_ID, 500L, TransactionType.USE, now)
        );

        when(pointHistoryTable.selectAllByUserId(USER_ID))
                .thenReturn(mockHistory);
        // userPointTable mock 추가
        List<PointHistory> result = pointService.history(USER_ID);

        assertEquals(2, result.size());

        assertEquals(TransactionType.CHARGE, result.get(0).type());
        assertEquals(1000L, result.get(0).amount());

        assertEquals(TransactionType.USE, result.get(1).type());
        assertEquals(500L, result.get(1).amount());
    }
    // [정상 케이스] 사용자의 포인트 이력이 없을 때 조회가 정상적으로 처리되는지 검증
    @Test
    void history_포인트_이력_없음_성공() {
        long now = System.currentTimeMillis();
        when(pointHistoryTable.selectAllByUserId(USER_ID))
                .thenReturn(List.of());
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 0L, now));

        List<PointHistory> result = pointService.history(USER_ID);
        assertEquals(0, result.size());
    }

//  history - 실패 케이스
    // [에러 케이스] 존재하지 않는 사용자의 포인트 이력 조회 시 예외가 발생하는지 검증
    @Test
    void history_존재하지_않는_사용자_조회_실패() {
        long invalidUserId = -1L;
        when(userPointTable.selectById(invalidUserId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.history(invalidUserId)
        );
        assertEquals(ServiceErrorMessages.USER_NOT_FOUND, exception.getMessage());
    }

}
