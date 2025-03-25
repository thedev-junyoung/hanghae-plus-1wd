package io.hhplus.tdd;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {
    public static long getStartOfTodayMillisKST() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        return LocalDate.now(zone)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli();
    }

    public static long getStartOfTomorrowMillisKST() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        return LocalDate.now(zone).plusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli();
    }

}
