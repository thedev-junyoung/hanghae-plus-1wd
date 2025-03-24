package io.hhplus.tdd.point;

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

    public UserPoint charge(long id, long amount) {
        return null;
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
}
