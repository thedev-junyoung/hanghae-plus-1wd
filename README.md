# Java 동시성 제어 방식 및 장단점

## 1. 프로세스 동기화 개요

프로세스 동기화는 여러 프로세스나 스레드가 공유 자원에 접근할 때 발생할 수 있는 문제를 해결하기 위한 메커니즘이다. 동시성 문제가 제대로 처리되지 않으면 다음과 같은 문제가 발생한다:

1. **데이터 불일치**: 여러 스레드가 동시에 같은 데이터를 수정하면 예측할 수 없는 결과 발생
2. **데이터 손실**: 한 스레드의 작업이 다른 스레드의 작업을 덮어쓸 수 있음
3. **교착상태(Deadlock)**: 여러 스레드가 서로 점유한 자원을 기다리며 무한히 대기하는 상태

예를 들어, 내 프로젝트에서 A와 B가 동시에 같은 사용자 ID로 50,000원을 충전하는 요청을 보냈을 때 Race Condition으로 인해 최종 잔고가 50,000원이 되거나 100,000원이 되는 불일치가 발생할 수 있다.

### 동기화가 필요한 조건

1. **임계 영역(Critical Section)**: 공유 자원에 접근하는 코드 블록
2. **경쟁 상태(Race Condition)**: 여러 스레드가 동시에 공유 데이터에 접근하여 결과가 접근 순서에 의존하는 상황
3. **선점(Preemption)**: 실행 중인 스레드가 다른 스레드에 의해 중단될 수 있는 상황

내 프로젝트에서 `UserPointTable`의 `insertOrUpdate` 메서드는 임계 영역이며, 여러 스레드(요청)가 동시에 이 메서드를 호출할 때 경쟁 상태가 발생할 수 있다.

### 임계 영역 문제 해결을 위한 세 가지 조건

1. **상호 배제(Mutual Exclusion)**: 한 번에 하나의 프로세스만 임계 영역에 진입할 수 있다.
2. **진행(Progress)**: 임계 영역에 프로세스가 없고 진입하려는 프로세스가 있다면, 진입 결정은 유한 시간 내에 이루어져야 한다.
3. **유한 대기(Bounded Waiting)**: 프로세스가 임계 영역에 진입하려고 요청한 후 다른 프로세스들이 임계 영역에 진입하는 횟수에 제한이 있어야 한다.

## 2. 대표적인 동기화 문제 및 해결 메커니즘

프로세스 동기화와 관련된 몇 가지 고전적인 문제들과 그 해결책을 살펴보면 동시성 제어의 기본 원리를 이해하는 데 도움이 된다.

### 2.1. Dining Philosophers Problem: 식사하는 철학자 문제

다섯 명의 철학자가 원형 테이블에 앉아 있고, 각 철학자 사이에 젓가락이 하나씩 놓여 있다. 철학자는 생각하거나 먹는 두 가지 상태만 가지며, 먹기 위해서는 양쪽의 두 젓가락을 모두 집어야 한다.

#### 문제점
- 모든 철학자가 동시에 왼쪽 젓가락을 집으면 오른쪽 젓가락을 영원히 집을 수 없는 교착 상태 발생
- 일부 철학자만 계속 식사하고 나머지는 계속 기다리는 기아 상태 발생 가능

#### 해결 방법
1. **리소스 계층화**: 각 젓가락에 번호를 부여하고, 항상 낮은 번호의 젓가락부터 집도록 함
2. **중재자 도입**: 최대 4명의 철학자만 테이블에 동시에 앉을 수 있도록 제한
3. **세마포어 활용**: 젓가락마다 세마포어를 할당하여 접근 제어
4. **타임아웃 도입**: 일정 시간 내에 두 젓가락을 모두 집지 못하면 이미 집은 젓가락을 내려놓고 다시 시도

### 2.2. Readers-Writers Problem: 독자-작가 문제

여러 프로세스가 공유 데이터에 접근할 때, 일부는 데이터를 읽기만 하고(독자) 일부는 데이터를 수정(작가)하는 상황.

#### 문제점
- 여러 독자가 동시에 읽는 것은 문제 없음
- 작가가 쓰는 동안 다른 프로세스의 접근은 금지되어야 함
- 독자 우선, 작가 우선, 또는 공정성 중 어떤 정책을 선택할 것인지 결정 필요

#### 해결 방법
1. **독자 우선**: 독자가 있으면 작가는 대기, 독자의 읽기 효율성 증가
2. **작가 우선**: 작가가 대기 중이면 새로운 독자는 대기, 작가의 기아 상태 방지
3. **공정 솔루션**: FIFO 큐를 사용하여 도착 순서대로 처리

### 2.3. Producer-Consumer Problem: 생산자-소비자 문제

생산자 프로세스는 데이터를 생성하여 버퍼에 저장하고, 소비자 프로세스는 버퍼에서 데이터를 꺼내 사용하는 상황.

#### 문제점
- 버퍼가 가득 찼을 때 생산자가 더 생산하려 하면 문제 발생
- 버퍼가 비었을 때 소비자가 소비하려 하면 문제 발생
- 생산자와 소비자가 동시에 버퍼에 접근하면 데이터 불일치 발생 가능

#### 해결 방법
1. **세마포어 활용**: 빈 공간과 채워진 공간을 세마포어로 관리
2. **모니터 활용**: 조건 변수를 이용해 버퍼 상태에 따라 프로세스 대기/재개
3. **BlockingQueue 활용**: Java의 경우 BlockingQueue 인터페이스를 구현한 클래스 사용

### 2.4. Peterson's Algorithm: 피터슨 알고리즘

두 프로세스 간의 상호 배제를 보장하기 위한 알고리즘으로, 플래그와 차례 변수를 사용한다.

#### 동작 원리
1. 각 프로세스는 자신의 플래그를 설정하여 임계 영역 진입 의사 표시
2. 차례 변수에 상대방 번호를 설정하여 상대방에게 우선권 부여
3. 상대방의 플래그와 차례 변수를 확인하여 진입 여부 결정

#### 장점
- 상호 배제, 진행, 유한 대기 조건을 모두 만족
- 바쁜 대기(busy waiting)를 사용하지만 구현이 단순

## 3. 자바에서의 동시성 제어 메커니즘

### 3.1. Synchronized 키워드

가장 기본적인 동기화 메커니즘으로, 메서드나 코드 블록에 적용할 수 있다.

```java
public synchronized void deposit(long amount) {
    balance += amount;
}

// 또는
public void deposit(long amount) {
    synchronized(this) {
        balance += amount;
    }
}
```

#### 장점
- 사용이 간단하고 직관적
- 자동으로 락의 획득과 해제 처리
- 상호 배제와 가시성 모두 보장

#### 단점
- 락의 세부 제어가 어려움
- 타임아웃 설정 불가능
- 블록 전체가 락으로 보호되어 성능 저하 가능성

### 3.2. Volatile 키워드

변수에 volatile 키워드를 사용하면 해당 변수의 읽기/쓰기가 메인 메모리에서 직접 이루어진다.

```java
private volatile boolean isRunning = true;
```

#### 장점
- 가시성 보장: 한 스레드의 변경사항이 다른 스레드에 즉시 보임
- synchronized보다 오버헤드가 적음
- Happens-Before 관계 설정 (메모리 순서 보장)

#### 단점
- 상호 배제를 보장하지 않음 (원자성 보장 X)
- 복합 연산(증가, 감소 등)에 적합하지 않음
- 락을 사용하지 않기 때문에 race condition 해결 불가

### 3.3. Atomic 변수 (java.util.concurrent.atomic)

원자적 연산을 제공하는 클래스들로, 락 없이도 스레드 안전한 연산이 가능하다.

```java
private AtomicLong balance = new AtomicLong(0);

public void deposit(long amount) {
    balance.addAndGet(amount);
}
```

#### 장점
- 락 없이도 원자적 연산 가능
- 성능이 좋음 (내부적으로 CAS(Compare-And-Swap) 사용)
- 단일 변수 연산에 최적화

#### 단점
- 복합적인 조건부 로직에는 부적합
- 여러 변수의 원자적 업데이트에는 사용하기 까다로움
- CAS 반복 시도로 인한 CPU 사용량 증가 가능성

### 3.4. ReentrantLock (java.util.concurrent.locks)

synchronized보다 더 유연한 락 메커니즘을 제공한다.

```java
private final Lock lock = new ReentrantLock();
private long balance = 0;

public void deposit(long amount) {
    lock.lock();
    try {
        balance += amount;
    } finally {
        lock.unlock();
    }
}
```

#### 장점
- 타임아웃 설정 가능 (tryLock)
- 인터럽트 가능 (lockInterruptibly)
- 공정성 설정 가능 (fairness parameter)
- 더 세밀한 제어 가능

#### 단점
- 명시적인 잠금/해제 필요 (finally 블록에서 항상 해제해야 함)
- synchronized보다 사용법이 복잡
- 실수로 락 해제를 누락할 가능성

### 3.5. ReadWriteLock (java.util.concurrent.locks)

읽기 작업과 쓰기 작업에 대해 서로 다른 락을 제공한다.

```java
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();
private long balance = 0;

public long getBalance() {
    readLock.lock();
    try {
        return balance;
    } finally {
        readLock.unlock();
    }
}

public void deposit(long amount) {
    writeLock.lock();
    try {
        balance += amount;
    } finally {
        writeLock.unlock();
    }
}
```

#### 장점
- 읽기 작업에 대한 병렬성 증가
- 읽기 작업이 많은 애플리케이션에서 성능 향상
- 쓰기 시에는 완전한 배타적 접근 보장

#### 단점
- 구현이 복잡함
- 리더/라이터 교착 상태 가능성
- 읽기 작업이 적은 경우 오버헤드만 증가할 수 있음

### 3.6. 세마포어(Semaphore)

세마포어는 가용한 리소스의 수를 나타내는 카운터를 사용하여 동시 접근을 제어한다.

```java
private final Semaphore semaphore = new Semaphore(1); // 1개의 허가만 제공 (이진 세마포어)

public void deposit(long amount) {
    try {
        semaphore.acquire(); // 세마포어 획득 시도
        try {
            balance += amount;
        } finally {
            semaphore.release(); // 세마포어 해제
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

#### 장점
- 여러 스레드가 동시에 자원에 접근하도록 제한 가능
- 카운트 기반이므로 뮤텍스보다 더 유연함
- 리소스 풀 관리에 적합

#### 단점
- 재진입(reentrant) 특성이 없음
- 소유권(ownership) 개념이 없음
- 사용이 복잡할 수 있음

### 3.7. ConcurrentHashMap

스레드 안전한 해시맵 구현으로, 락 분할(lock striping) 기법을 사용하여 동시성을 높인다.

```java
private final ConcurrentHashMap<Long, AtomicLong> userBalances = new ConcurrentHashMap<>();

public void deposit(long userId, long amount) {
    AtomicLong balance = userBalances.computeIfAbsent(userId, k -> new AtomicLong(0));
    balance.addAndGet(amount);
}
```

#### 장점
- 높은 동시성: 여러 스레드가 동시에 다른 버킷의 데이터에 접근 가능
- 락 분할로 인한 성능 향상
- null 값 허용하지 않음으로써 NPE 방지

#### 단점
- 전체 맵에 대한 단일 작업(atomic operations)을 보장하지 않음
- 일부 복합 연산은 추가 동기화 필요
- 메모리 사용량이 더 많을 수 있음

## 4. 내 프로젝트에 적용한 동시성 제어 방식

내 프로젝트에서는 포인트 충전/사용 기능을 구현했으며, 동일 사용자에 대한 동시 요청 처리가 필요하다. 코드를 보면 `PointService` 클래스에서 포인트 충전 및 사용 메서드가 구현되어 있는데, 이 부분에 ReentrantLock과 ConcurrentHashMap을 결합한 동시성 제어 방식을 적용했다.

### 4.1. ReentrantLock과 ConcurrentHashMap을 활용한 사용자별 락

사용자 ID 기반의 세밀한 락 제어를 위해 `ReentrantLock`과 `ConcurrentHashMap`을 조합하여 구현했다. 각 사용자 ID에 대해 독립적인 락을 생성하여 다른 사용자의 요청은 서로 간섭 없이 병렬 처리된다.

```java
// 락 사용 시간을 추적하기 위한 구조체
private static class LockWithTimestamp {
    final ReentrantLock lock = new ReentrantLock();
    volatile long lastAccessTime;

    LockWithTimestamp() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    void updateAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }
}

// 사용자별 락 관리
private final ConcurrentHashMap<Long, LockWithTimestamp> userLocks = new ConcurrentHashMap<>();

// 락 객체를 가져오고 사용 시간 업데이트
private ReentrantLock getUserLock(long userId) {
    LockWithTimestamp lockWithTimestamp = userLocks.computeIfAbsent(userId,
            k -> new LockWithTimestamp());
    lockWithTimestamp.updateAccessTime();
    return lockWithTimestamp.lock;
}
```

이 구현에서는 사용자 ID를 키로 하여 락 객체를 관리하고, 각 락 객체에는 마지막 사용 시간을 함께 저장하여 메모리 관리를 효율적으로 할 수 있다.

### 4.2. 미사용 락 객체 정리 메커니즘

메모리 누수를 방지하기 위해 일정 시간 동안 사용되지 않은 락 객체를 주기적으로 정리하는 메커니즘을 구현했다.

```java
// 주기적으로 사용되지 않는 락 객체 정리
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
```

이 메커니즘은 다음과 같은 특징을 가진다:

1. 스프링의 `@Scheduled` 어노테이션을 사용하여 1시간마다 자동 실행
2. 24시간 동안 사용되지 않은 락 객체만 제거하여 불필요한 메모리 사용 방지
3. 현재 사용 중인 락은 제거하지 않아 동시성 문제 방지
4. 로깅을 통해 현재 유지 중인 락 개수 모니터링 가능

### 4.3. 포인트 충전/사용 메서드에 적용

포인트 충전 메서드에 사용자별 락을 적용한 예:

```java
@Override
public UserPoint charge(long id, long amount) {
    ReentrantLock lock = getUserLock(id);
    lock.lock();
    try {
        // 사용자 존재 확인
        UserPoint userPoint = findUserPointOrThrow(id);

        // 도메인 객체 생성
        ChargeAmount chargeAmount = ChargeAmount.validated(amount);
        Point currentPoint = Point.of(userPoint.point());

        // 일일 충전 한도 검증
        long todayTotal = getTodayChargeAmount(id);
        if (todayTotal + amount > PointPolicy.DAILY_CHARGE_LIMIT) {
            throw new IllegalArgumentException(ServiceErrorMessages.DAILY_CHARGE_LIMIT);
        }

        // 실제 충전 (도메인 로직 수행)
        Point newBalance = currentPoint.charge(chargeAmount);

        // DB 업데이트
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, newBalance.value());

        // 충전 이력 저장
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, timeProvider.getCurrentTimeMillis());

        return updatedUserPoint;
    } finally {
        lock.unlock();
    }
}
```

## 5. 동시성 제어 방식의 장단점 및 선택 이유

### 장점

1. **사용자별 독립적인 락**: 서로 다른 사용자에 대한 요청은 병렬 처리되므로 전체 시스템 처리량 증가
2. **세밀한 락 제어**: ReentrantLock을 통해 타임아웃, 인터럽트 등 활용 가능
3. **메모리 효율성**: LockWithTimestamp 클래스와 주기적인 정리 메커니즘으로 메모리 사용 최적화
4. **교착상태 방지**: 항상 같은 순서로 락을 획득하므로 교착상태 발생 가능성 최소화

### 단점

1. **추가 복잡성**: 기본 synchronized보다 구현이 복잡함
2. **명시적 락 해제**: unlock() 호출이 필요하여 finally 블록 처리 필수
3. **추가 메모리 사용**: 각 사용자별 락 객체 생성으로 인한 추가 메모리 사용

### 선택 이유

포인트 충전/사용 시스템에서는 다음과 같은 특성이 있기 때문에 ReentrantLock과 ConcurrentHashMap을 결합한 방식을 선택했다:

1. **다중 단계 트랜잭션**: 포인트 충전/사용은 여러 단계(검증, 계산, DB 업데이트 등)를 거치므로 원자적 연산만으로는 불충분
2. **사용자별 독립성**: 서로 다른 사용자의 트랜잭션은 상호 간섭 없이 병렬 처리되어야 함
3. **동시 요청 처리**: 동일 사용자에 대한 동시 요청은 직렬화하여 일관성 보장 필요
4. **효율적인 메모리 관리**: 사용자 수가 많아질 경우 메모리 효율성이 중요

## 6. 결론

Java에서는 다양한 동시성 제어 메커니즘을 제공하며, 애플리케이션의 요구사항에 따라 적절한 방식을 선택해야 한다. 내 포인트 서비스에서는 사용자별 독립적인 락을 제공하면서도 세밀한 제어가 가능한 ReentrantLock과 ConcurrentHashMap의 조합이 가장 적합하다고 판단했다.

추가로 구현한 미사용 락 정리 메커니즘을 통해 메모리 효율성을 높임으로써, 대규모 사용자를 처리하는 상황에서도 안정적인 동시성 제어가 가능하다. 이 방식은 성능과 정확성 사이의 균형을 맞추는 데 효과적이며, 과도한 동기화로 인한 성능 저하와 불충분한 동기화로 인한 데이터 불일치 문제를 모두 해결할 수 있다.



## 참고 문서
- [Java Concurrency - Oracle Docs](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)
- [Concurrent Collections](https://docs.oracle.com/javase/tutorial/essential/concurrency/collections.html)
- [Atomic Variables](https://docs.oracle.com/javase/tutorial/essential/concurrency/atomicvars.html)
- [Happens-Before Relationship in Java](https://www.geeksforgeeks.org/happens-before-relationship-in-java/)
- [Dining Philosophers Problem](https://www.geeksforgeeks.org/dining-philosopher-problem-using-semaphores/)
- [Readers-Writers Problem - Readers Preference](https://www.geeksforgeeks.org/readers-writers-problem-set-1-introduction-and-readers-preference-solution/)
- [Readers-Writers Problem - Writers Preference](https://www.geeksforgeeks.org/readers-writers-problem-writers-preference-solution/)
- [Java Volatile Keyword - Baeldung](https://www.baeldung.com/java-volatile)
- [ReentrantReadWriteLock - GeeksforGeeks](https://www.geeksforgeeks.org/reentrantreadwritelock-class-in-java/)
- [Java Locks API - Oracle](https://docs.oracle.com/javase/tutorial/essential/concurrency/newlocks.html)
