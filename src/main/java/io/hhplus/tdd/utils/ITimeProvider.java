package io.hhplus.tdd.utils;

public interface ITimeProvider {
    long getCurrentTimeMillis();
    long getStartOfTodayMillis();
    long getStartOfTomorrowMillis();
}
