package io.hhplus.tdd.domain.point.service;

import io.hhplus.tdd.domain.point.service.lock.UserLockManager;
import io.hhplus.tdd.infrastructure.time.ITimeProvider;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import io.hhplus.tdd.domain.point.model.UserPoint;
import io.hhplus.tdd.domain.point.vo.UseAmount;
import io.hhplus.tdd.domain.point.vo.ChargeAmount;
import io.hhplus.tdd.domain.point.vo.Point;
import io.hhplus.tdd.domain.point.policy.PointPolicy;
import io.hhplus.tdd.domain.point.error.ServiceErrorMessages;
import io.hhplus.tdd.infrastructure.database.PointHistoryTable;
import io.hhplus.tdd.infrastructure.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 포인트 서비스 설계
 * 1. 유효성 검증 책임 분리
 *    - 클라이언트로부터의 입력 유효성 검증은 Controller 단에서 수행합니다.
 *    - Spring Validator 사용에 제약이 있어 AssertUtil 유틸 클래스를 통해 대체하였습니다.
 * 2. 비즈니스 로직 책임 분리
 *    - 서비스 클래스는 사용자 잔액 조회, 일일 충전/사용 한도 정책 검증, DB 처리 등
 *      상태 기반의 비즈니스 로직을 담당합니다.
 * 3. 도메인 로직 캡슐화
 *    - `vo` 패키지에는 도메인 값 객체(VO: Value Object)를 정의하여,
 *      포인트 사용/충전과 같은 핵심 비즈니스 규칙을 VO 내부에서 보장합니다.
 *    - 예) ChargeAmount, UseAmount, Point 등은 객체 생성 시점에 유효성 검사를 수행합니다.
 * 이를 통해 각 레이어가 아래의 책임만을 가지도록 설계하였습니다:
 *    - Controller: 입력값 기본 검증
 *    - Service: 상태 기반 정책 및 DB 연동
 *    - VO: 도메인 규칙 (도메인 유효성) 검증
 * 또한 테스트 가능한 구조를 위해 시간 의존 로직은 `ITimeProvider` 인터페이스로 추상화하고,
 * 실제 환경에서는 `KSTTimeProvider`를 사용하여 한국 표준시에 맞는 일일 기준 처리가 가능하도록 구현하였습니다.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PointService implements IPointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ITimeProvider timeProvider;
    private final UserLockManager lockManager;

    @Override
    public UserPoint charge(long id, long amount) {
        ReentrantLock lock = lockManager.getUserLock(id);
        lock.lock();
        try {
            // 사용자 존재 확인
            UserPoint userPoint = findUserPointOrThrow(id);

            // 도메인 객체 생성
            ChargeAmount chargeAmount = ChargeAmount.validated(amount);
            Point currentPoint = Point.of(userPoint.point());

            // 일일 충전 한도 검증
            long todayTotal = getTodayChargeAmount(id);
            if (todayTotal + amount > PointPolicy.DAILY_CHARGE_LIMIT) {
                throw new IllegalArgumentException(ServiceErrorMessages.DAILY_CHARGE_LIMIT);
            }

            // 실제 충전 (도메인 로직 수행)
            Point newBalance = currentPoint.charge(chargeAmount);

            // DB 업데이트
            UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, newBalance.value());

            // 충전 이력 저장
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, timeProvider.getCurrentTimeMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public UserPoint use(long id, long amount) {
        ReentrantLock lock = lockManager.getUserLock(id);
        lock.lock();
        try {
            // 사용자 존재 확인
            UserPoint userPoint = findUserPointOrThrow(id);

            // 도메인 객체 생성
            UseAmount useAmount = UseAmount.validated(amount);
            Point currentPoint = Point.of(userPoint.point());

            // 일일 사용 한도 검증
            long todayUsedAmount = getTodayUsedAmount(id);
            if (todayUsedAmount + useAmount.value() > PointPolicy.MAX_USE_AMOUNT_PER_DAY) {
                throw new IllegalArgumentException(ServiceErrorMessages.MAX_USE_AMOUNT_PER_DAY);
            }

            // 실제 포인트 사용 (도메인 로직 수행)
            Point newBalance = currentPoint.use(useAmount);

            // DB 업데이트
            UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, newBalance.value());

            // 사용 이력 저장
            pointHistoryTable.insert(id, useAmount.value(), TransactionType.USE, timeProvider.getCurrentTimeMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserPoint point(long id) {
        return findUserPointOrThrow(id);
    }

    @Override
    public List<PointHistory> history(long id) {
        UserPoint userPoint = findUserPointOrThrow(id);
        return pointHistoryTable.selectAllByUserId(userPoint.id());
    }

//  ================================================================

    // 유저 포인트 조회
    private UserPoint findUserPointOrThrow(long id) {
        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint == null) {
            throw new IllegalArgumentException(ServiceErrorMessages.USER_NOT_FOUND);
        }
        return userPoint;
    }

    // 유저의 하루 충전 포인트 이력 조회
    private long getTodayChargeAmount(Long id) {
        List<PointHistory> allHistories = pointHistoryTable.selectAllByUserId(id);

        long start = timeProvider.getStartOfTodayMillis();
        long end = timeProvider.getStartOfTomorrowMillis();

        return allHistories.stream()
                .filter(history -> history.updateMillis() >= start && history.updateMillis() < end)
                .filter(history -> history.type() == TransactionType.CHARGE)
                .mapToLong(PointHistory::amount)
                .sum();
    }

    // 하루 사용 포인트 이력 조회
    private long getTodayUsedAmount(Long id) {
        List<PointHistory> allHistories = pointHistoryTable.selectAllByUserId(id);

        long start = timeProvider.getStartOfTodayMillis();
        long end = timeProvider.getStartOfTomorrowMillis();

        return allHistories.stream()
                .filter(history -> history.updateMillis() >= start && history.updateMillis() < end)
                .filter(history -> history.type() == TransactionType.USE)
                .mapToLong(PointHistory::amount)
                .sum();
    }

}
