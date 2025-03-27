package io.hhplus.tdd.domain.point.service.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class UserLockManager {

    private final ConcurrentHashMap<Long, LockWithTimestamp> userLocks = new ConcurrentHashMap<>();
    private final long expirationMillis;

    public UserLockManager(long expirationMillis) {
        this.expirationMillis = expirationMillis;
    }

    public ReentrantLock getUserLock(Long userId) {
        LockWithTimestamp lockWithTimestamp = userLocks.computeIfAbsent(userId, id -> new LockWithTimestamp());
        lockWithTimestamp.updateAccessTime();
        return lockWithTimestamp.getLock();
    }

    public void cleanupUnusedLocks() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<Long, LockWithTimestamp>> it = userLocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, LockWithTimestamp> entry = it.next();
            LockWithTimestamp lwt = entry.getValue();

            if (!lwt.getLock().isLocked() &&
                    now - lwt.getLastAccessTime() > expirationMillis) {
                it.remove();
            }
        }

        log.info("Lock cleanup completed. Remaining locks: {}", userLocks.size());
    }

    public boolean hasLockForUser(Long userId) {
        return userLocks.containsKey(userId);
    }

}
