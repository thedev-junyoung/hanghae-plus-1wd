package io.hhplus.tdd.domain.point.service;

import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.UserPoint;

import java.util.List;

public interface IPointService {
    UserPoint charge(long id, long amount);
    UserPoint use(long id, long amount);
    UserPoint point(long id);
    List<PointHistory> history(long id);


}
