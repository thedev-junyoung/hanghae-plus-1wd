package io.hhplus.tdd.infrastructure.time;

/**
 * 시간 처리 추상화 인터페이스
 * 시간에 의존하는 로직(일별 한도 등)을 테스트하기 용이하게 만들기 추상화.
 * 인터페이스로 분리한 이유:
 * 1. 관심사 분리 - 시간 관련 로직을 핵심 비즈니스 로직과 분리
 * 2. 확장성 - 다양한 시간대나 특수 환경에 대응 가능
 */
public interface ITimeProvider {
    long getCurrentTimeMillis();
    long getStartOfTodayMillis();
    long getStartOfTomorrowMillis();
}
