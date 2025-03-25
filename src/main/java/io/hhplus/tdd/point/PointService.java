package io.hhplus.tdd.point;

import io.hhplus.tdd.AssertUtil;
import io.hhplus.tdd.policy.PointErrorMessages;
import io.hhplus.tdd.policy.PointPolicy;
import io.hhplus.tdd.TimeUtil;
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

//   1. 포인트 충전
    public UserPoint charge(long id, long amount) {
        // 포인트 충전, 기록 저장

        // id, amount 검증
        AssertUtil.requirePositive(amount, PointErrorMessages.AMOUNT_NEGATIVE);
        AssertUtil.requirePositive(id, PointErrorMessages.USER_NEGATIVE_ID);

        // 최소 충전 금액 검증
        if (amount < PointPolicy.MIN_CHARGE_AMOUNT)
            throw new IllegalArgumentException(PointErrorMessages.MIN_CHARGE);

        // 최대 충전 금액 검증
        if (amount > PointPolicy.MAX_CHARGE_AMOUNT)
            throw new IllegalArgumentException(PointErrorMessages.MAX_CHARGE);

        // 하루 최대 충전 한도 검증
        long todayTotal = todayChargeAmount(id);
        if (todayTotal + amount > PointPolicy.DAILY_CHARGE_LIMIT)
            throw new IllegalArgumentException(PointErrorMessages.DAILY_CHARGE_LIMIT);

        // 잔액 검증
        long userBalance = getUserPointBalance(id);
        if (userBalance < amount) throw new IllegalArgumentException(PointErrorMessages.INSUFFICIENT_BALANCE);



        UserPoint userPoint = userPointTable.insertOrUpdate(id, amount);
        PointHistory pointHistory = pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());


        return userPoint;
    }

    public UserPoint use(long id, long amount) {
        return null;
    }

    public UserPoint point(long id) {
        return null;
    }

    public List<PointHistory> history(long id) {
        return null;
    }



//  유저의 하루 충전 포인트 이력 조회
    public long todayChargeAmount(long id) {
        List<PointHistory> allHistories = pointHistoryTable.selectAllByUserId(id);

        long start = TimeUtil.getStartOfTodayMillisKST();
        long end = TimeUtil.getStartOfTomorrowMillisKST(); // exclusive

        return allHistories.stream()
                .filter(history -> history.updateMillis() >= start && history.updateMillis() < end)
                .filter(history -> history.type() == TransactionType.CHARGE)
                .mapToLong(PointHistory::amount)
                .sum();
    }

//    유저 잔액 조회
    public long getUserPointBalance(long id) {
        return userPointTable.selectById(id).point();
    }


}
