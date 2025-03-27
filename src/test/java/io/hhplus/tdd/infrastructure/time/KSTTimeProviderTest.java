package io.hhplus.tdd.infrastructure.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KSTTimeProvider 테스트
 * 한국 표준시(Asia/Seoul) 기준 시간 관련 기능 테스트
 */
class KSTTimeProviderTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private final KSTTimeProvider timeProvider = new KSTTimeProvider();
    /**
     * [기능 검증] 현재 시간을 밀리초 단위로 반환하는 기능이
     * 실제 시스템 시간(KST 기준)과 유사한지 검증
     * - 시간은 계속 흐르기 때문에 1초 이내 오차를 허용
     * - 시간 의존 로직 테스트의 신뢰성을 보장하기 위함
     */
    @Test
    void getCurrentTimeMillis_현재시간_반환_성공() {
        // given
        long currentTimeMillis = ZonedDateTime.now(ZONE).toInstant().toEpochMilli();

        // when
        long result = timeProvider.getCurrentTimeMillis();

        // then
        // 시간은 계속 흐르므로 정확한 일치보다는 근사값 비교
        assertTrue(Math.abs(result - currentTimeMillis) < 1000, "현재 시간은 1초 이내의 오차여야 합니다");
    }
    /**
     * [정책 검증] 오늘(한국시간)의 0시를 기준으로 반환되는 시간이
     * 예상대로 정확한 밀리초 값인지 검증
     * - 포인트 일일 한도 정책 기준 시점 검증용
     */
    @Test
    void getStartOfTodayMillis_오늘자정시간_반환_성공() {
        // given
        long expectedStartOfDay = LocalDate.now(ZONE)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();

        // when
        long result = timeProvider.getStartOfTodayMillis();

        // then
        assertEquals(expectedStartOfDay, result, "오늘의 시작 시간(자정)이 올바르게 계산되어야 함");
    }
    /**
     * [정책 검증] 내일(한국시간)의 0시를 기준으로 반환되는 시간이
     * 정확한 밀리초 값인지 검증
     * - 일별 한도 계산 시 "오늘~내일" 범위 체크용 기준 검증
     */
    @Test
    void getStartOfTomorrowMillis_내일자정시간_반환_성공() {
        // given
        long expectedStartOfTomorrow = LocalDate.now(ZONE).plusDays(1)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();

        // when
        long result = timeProvider.getStartOfTomorrowMillis();

        // then
        assertEquals(expectedStartOfTomorrow, result, "내일의 시작 시간(자정)이 올바르게 계산되어야 함");
    }
    /**
     * [단위 시간 검증] 오늘 자정과 내일 자정 간의 차이가
     * 정확히 24시간(밀리초)인지 검증
     * - 일 단위 시간 범위가 잘못 계산될 가능성을 방지
     */
    @Test
    void 하루시간단위_24시간_확인() {
        // given
        long startOfToday = timeProvider.getStartOfTodayMillis();
        long startOfTomorrow = timeProvider.getStartOfTomorrowMillis();

        // when
        long oneDayMillis = startOfTomorrow - startOfToday;

        // then
        assertEquals(24 * 60 * 60 * 1000, oneDayMillis, "하루는 24시간(밀리초)이어야 함");
    }
}