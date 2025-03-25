package io.hhplus.tdd.point;

import io.hhplus.tdd.point.vo.UseAmount;
import io.hhplus.tdd.utils.AssertUtil;
import io.hhplus.tdd.point.vo.ChargeAmount;
import io.hhplus.tdd.point.vo.Point;
import io.hhplus.tdd.point.vo.UserId;
import io.hhplus.tdd.policy.PointErrorMessages;
import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.utils.ITimeProvider;
import io.hhplus.tdd.utils.KSTTimeProvider;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ITimeProvider timeProvider;
//   1. 포인트 충전
    public UserPoint charge(long id, long amount) {
        // 기본 검증
        AssertUtil.requirePositive(amount, PointErrorMessages.AMOUNT_NEGATIVE);
        AssertUtil.requirePositive(id, PointErrorMessages.USER_NEGATIVE_ID);

        UserId userId = new UserId(id); // 선택사항


        // 충전 금액 객체 생성(도메인에 검증 로직 위임)
        ChargeAmount chargeAmount = new ChargeAmount(amount);

        // 잔액 및 오늘 충전 금액 조회
        Point current = new Point(getUserPointBalance(userId));
        long todayTotal = todayChargeAmount(userId);

        // 정책 검증
        if (todayTotal + amount > PointPolicy.DAILY_CHARGE_LIMIT) {
            throw new IllegalArgumentException(PointErrorMessages.DAILY_CHARGE_LIMIT);
        }

        // 실제 충전
        Point newBalance = current.charge(chargeAmount);
        UserPoint userPoint = userPointTable.insertOrUpdate(userId.value(), newBalance.value());

        // 충전 기록 저장
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPoint;
    }


    public UserPoint use(long id, long amount) {
        UserId userId = new UserId(id);
        UseAmount useAmount = new UseAmount(amount); // 1회 사용 금액 & 최소 사용 금액 검증

        // 하루 최대 사용 금액 확인
        long todayUsedAmount = getTodayUsedAmount(userId);
        if (todayUsedAmount + amount > PointPolicy.MAX_USE_AMOUNT_PER_DAY) {
            throw new IllegalArgumentException(PointErrorMessages.MAX_USE_AMOUNT_PER_DAY);
        }

        long currentBalance = getUserPointBalance(userId);
        Point point = new Point(currentBalance);
        Point newBalance = point.use(useAmount);

        UserPoint userPoint = userPointTable.insertOrUpdate(userId.value(), newBalance.value());
        PointHistory pointHistory = pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPoint;
    }

    public UserPoint point(long id) {
        return null;
    }

    public List<PointHistory> history(long id) {
        return null;
    }



//  유저의 하루 충전 포인트 이력 조회
    public long todayChargeAmount(UserId id) {
        List<PointHistory> allHistories = pointHistoryTable.selectAllByUserId(id.value());

        long start = timeProvider.getStartOfTodayMillis();
        long end = timeProvider.getStartOfTomorrowMillis();

        return allHistories.stream()
                .filter(history -> history.updateMillis() >= start && history.updateMillis() < end)
                .filter(history -> history.type() == TransactionType.CHARGE)
                .mapToLong(PointHistory::amount)
                .sum();
    }

//    ================================================================
    // 유저 잔액 조회
    public long getUserPointBalance(UserId id) {
        return userPointTable.selectById(id.value()).point();
    }

    public long getTodayUsedAmount(UserId id) {
        List<PointHistory> allHistories = pointHistoryTable.selectAllByUserId(id.value());

        long start = timeProvider.getStartOfTodayMillis();
        long end = timeProvider.getStartOfTomorrowMillis();

        return allHistories.stream()
                .filter(history -> history.updateMillis() >= start && history.updateMillis() < end)
                .filter(history -> history.type() == TransactionType.USE)
                .mapToLong(PointHistory::amount)
                .sum();
    }


}
