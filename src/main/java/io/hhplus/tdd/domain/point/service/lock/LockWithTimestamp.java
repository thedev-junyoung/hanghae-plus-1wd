package io.hhplus.tdd.domain.point.service.lock;

import lombok.Getter;

import java.util.concurrent.locks.ReentrantLock;

@Getter
public class LockWithTimestamp {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile long lastAccessTime;

    public LockWithTimestamp() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public void updateAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }

}
