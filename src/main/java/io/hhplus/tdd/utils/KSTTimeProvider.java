package io.hhplus.tdd.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;


@Component // 스프링 환경에서 사용시
public class KSTTimeProvider implements ITimeProvider {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public long getCurrentTimeMillis() {
        return ZonedDateTime.now(ZONE).toInstant().toEpochMilli();
    }

    @Override
    public long getStartOfTodayMillis() {
        return LocalDate.now(ZONE)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();
    }

    @Override
    public long getStartOfTomorrowMillis() {
        return LocalDate.now(ZONE).plusDays(1)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();
    }
}