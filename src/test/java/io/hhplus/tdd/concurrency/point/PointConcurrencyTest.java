package io.hhplus.tdd.concurrency.point;

import io.hhplus.tdd.domain.point.model.UserPoint;
import io.hhplus.tdd.domain.point.service.IPointService;
import io.hhplus.tdd.infrastructure.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointConcurrencyTest {

    @Autowired
    private IPointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    private final long userId = 1L;

    @BeforeEach
    void setup() {
        userPointTable.insertOrUpdate(userId, 0);
    }

    @Test
    void 동일_사용자_동시_충전_테스트() throws Exception {
        long chargeAmount = 50000;
        int threadCount = 2;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.charge(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        UserPoint result = pointService.point(userId);
        assertThat(result.point()).isEqualTo(chargeAmount * threadCount);
    }
}
