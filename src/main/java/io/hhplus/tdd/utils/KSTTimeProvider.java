package io.hhplus.tdd.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;



/**
 * 한국 표준시 기반 시간 제공자
 *
 * 비즈니스 요구사항에 따른 한국 시간(KST)을 기준으로 시간 정보 제공
 * 일별 포인트 충전/사용 한도를 정확히 계산하기 위해 한국 시간대의 자정을 기준으로
 * 하루의 시작과 끝을 결정
 *
 * 별도 구현체로 분리한 이유:
 * 1. 단일 책임 원칙 - 시간 제공이라는 한 가지 책임만 담당
 * 2. 지역화 요구사항 - 한국 시간대 기준 비즈니스 규칙 적용
 * 3. 의존성 주입 - 스프링 환경에서 다른 컴포넌트에 주입하여 사용
 */
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