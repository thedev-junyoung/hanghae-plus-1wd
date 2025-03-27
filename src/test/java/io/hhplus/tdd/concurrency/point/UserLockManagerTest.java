package io.hhplus.tdd.concurrency.point;

import io.hhplus.tdd.domain.point.service.lock.UserLockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLockManager 테스트")
class UserLockManagerTest {

    private UserLockManager lockManager;

    @BeforeEach
    void setUp() {
        // 테스트에서는 50ms를 만료 기준으로 사용 (짧게 설정해야 테스트 가능)
        lockManager = new UserLockManager(50L);
    }

    @Test
    void 동일한_유저ID는_항상_동일한_락을_반환한다() {
        ReentrantLock lock1 = lockManager.getUserLock(1L);
        ReentrantLock lock2 = lockManager.getUserLock(1L);
        assertThat(lock1).isSameAs(lock2);
    }

    @Test
    void 서로_다른_유저ID는_서로_다른_락을_반환한다() {
        ReentrantLock lock1 = lockManager.getUserLock(1L);
        ReentrantLock lock2 = lockManager.getUserLock(2L);
        assertThat(lock1).isNotSameAs(lock2);
    }

    @Test
    void cleanupUnusedLocks는_사용_중이_아닌_오래된_락만_제거한다() throws Exception {
        long oldUserId = 10L;
        long activeUserId = 20L;

        // 두 락 등록
        ReentrantLock oldLock = lockManager.getUserLock(oldUserId);
        ReentrantLock activeLock = lockManager.getUserLock(activeUserId);

        // oldLock의 시간 조작 (lastAccessTime을 강제로 오래전으로)
        Thread.sleep(60); // 50ms 초과되도록 대기

        // activeLock은 마지막 접근 시간 업데이트 (최신처럼 유지)
        lockManager.getUserLock(activeUserId);

        // 락 제거 시도
        lockManager.cleanupUnusedLocks();

        // 오래된 락은 제거되고, 최근 접근 락은 유지됨
        assertThat(lockManager.hasLockForUser(oldUserId)).isFalse();
        assertThat(lockManager.hasLockForUser(activeUserId)).isTrue();
    }

    @Test
    void 사용중인_락은_만료되어도_제거되지_않는다() throws Exception {
        long userId = 123L;
        ReentrantLock lock = lockManager.getUserLock(userId);

        // 오래된 락처럼 만들기 위해 대기
        Thread.sleep(60);

        // 락을 사용 중인 상태로 유지
        lock.lock();
        try {
            lockManager.cleanupUnusedLocks();
            assertThat(lockManager.hasLockForUser(userId)).isTrue(); // 제거되지 않음
        } finally {
            lock.unlock();
        }

        // 락 해제 후, 다시 정리하면 제거되어야 함
        Thread.sleep(10);
        lockManager.cleanupUnusedLocks();
        assertThat(lockManager.hasLockForUser(userId)).isFalse(); // 제거됨
    }

    @Test
    void 다중_스레드에서_동일한_락을_사용해도_문제없이_동작한다() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ReentrantLock lock = lockManager.getUserLock(42L);
                lock.lock();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(lockManager.hasLockForUser(42L)).isTrue(); // 락 존재 확인
    }
}
