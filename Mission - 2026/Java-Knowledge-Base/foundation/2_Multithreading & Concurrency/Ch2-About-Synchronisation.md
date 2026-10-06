# Java Concurrency — Synchronization, Monitor, `synchronized`, Reentrancy

## Quick Revision — Read This First

- **Concurrency problem starts with shared mutable state.**
- Shared mutable state accessed by multiple threads can cause a **race condition**.
- The code that must be protected is called a **critical section**.
- **Synchronization** is used to control access to that critical section.
- In Java, every object can be used as a **monitor**.
- `synchronized` uses the object's **intrinsic monitor lock**.
- A Java monitor provides:
    - **Mutual exclusion**
    - **Thread coordination**
    - Lock ownership
    - Reentrant lock count
    - Threads waiting to acquire the monitor
    - A separate **wait set** for threads that call `wait()`
- `synchronized` provides:
    - Mutual exclusion
    - Memory visibility / happens-before guarantees
    - Reentrancy
- **`synchronized` itself is reentrant.**
- `ReentrantLock` is a separate explicit lock API with extra features such as:
    - `tryLock()`
    - timed lock attempts
    - interruptible locking
    - fairness
    - multiple `Condition`s
- Atomic classes such as `AtomicInteger` are useful for simple atomic operations.
- Atomic variables do **not automatically protect multi-step business invariants**.

---

# 1. Overall Flow

```text
Concurrent Environment
        ↓
Multiple Threads
        ↓
Shared Mutable State
        ↓
Race Condition
        ↓
Need Synchronization
        ↓
Protect Critical Section
        ↓
Synchronization Mechanisms
        |
        ├── synchronized
        │      ↓
        │   Object Monitor / Intrinsic Lock
        │
        ├── Explicit Locks
        │      ├── ReentrantLock
        │      ├── ReadWriteLock
        │      └── StampedLock
        │
        └── Atomic Operations
               ├── AtomicInteger
               ├── AtomicLong
               └── AtomicReference
```

---

# 2. What Is Synchronization?

Synchronization is used to safely coordinate multiple threads when they access shared mutable state.

Example problem:

```java
balance++;
```

This looks like one line, but internally it is roughly:

```text
read balance
    ↓
add 1
    ↓
write balance
```

If two threads perform this at the same time, one update can be lost.

That is a **race condition**.

Synchronization protects the relevant critical section so that the operation is performed safely.

---

# 3. What Is a Critical Section?

A **critical section** is the part of code that accesses shared mutable state and must not execute concurrently in an unsafe way.

Example:

```java
class Account {

    private int balance = 100;

    public synchronized void withdraw(int amount) {

        if (balance >= amount) {
            balance -= amount;
        }
    }
}
```

The critical operation is not only:

```java
balance -= amount;
```

The complete sequence matters:

```text
check balance
    ↓
decide whether withdrawal is allowed
    ↓
update balance
```

The full operation must be protected because it represents one business invariant.

---

# 4. What Is a Monitor?

A Java object can act as a **monitor**.

Example:

```java
Object lock = new Object();

synchronized (lock) {
    // critical section
}
```

Here:

```text
lock
```

is the monitor object.

The monitor conceptually manages:

- Which thread currently owns its intrinsic lock
- Whether the lock is free
- Reentrant acquisition count
- Threads blocked while trying to acquire the monitor
- Threads that called `wait()` and entered the monitor's wait set

---

# 5. Monitor = More Than Just a Lock

A monitor provides two major capabilities:

```text
Monitor
   |
   ├── Mutual Exclusion
   |
   └── Thread Coordination
```

## Mutual Exclusion

Only one thread can own the monitor at a time.

## Thread Coordination

Java provides:

```java
wait();
notify();
notifyAll();
```

These allow threads to coordinate around a condition.

---

# 6. `synchronized`

Example:

```java
synchronized (lock) {

    balance++;
}
```

Meaning:

```text
Acquire lock object's monitor
        ↓
Execute critical section
        ↓
Release monitor
```

Suppose two threads execute the same block.

```text
T1 reaches synchronized block
        ↓
T1 acquires monitor
        ↓
T1 executes critical section
```

Meanwhile:

```text
T2 reaches same synchronized block
        ↓
monitor already owned by T1
        ↓
T2 becomes BLOCKED
```

After T1 exits:

```text
T1 releases monitor
        ↓
one of the competing threads may acquire it
        ↓
T2 can continue
```

---

# 7. What Does `synchronized` Guarantee?

Do not remember `synchronized` as only a lock.

Remember:

```text
synchronized
      =
Mutual Exclusion
      +
Memory Visibility / Ordering
      +
Reentrancy
```

## Mutual Exclusion

Only one thread executes the protected critical section for the same monitor at a time.

## Memory Visibility

Changes made by a thread before releasing a monitor become visible to a thread that subsequently acquires the same monitor.

This comes from the Java Memory Model's **happens-before** rules.

---

# 8. Monitor Waiting Threads — Important Distinction

There are two different categories of waiting threads.

```text
                    Object Monitor
                         |
          +--------------+--------------+
          |                             |
     Entry / Blocked Set              Wait Set
          |                             |
Threads trying to                 Threads that called
acquire monitor                   wait()
```

These should not be treated as the same thing.

---

# 9. What Happens When a Thread Calls `wait()`?

Example:

```java
synchronized (lock) {

    while (!condition) {
        lock.wait();
    }

    // continue processing
}
```

Flow:

```text
Thread owns monitor
      ↓
calls wait()
      ↓
releases monitor
      ↓
enters monitor's wait set
```

This is important:

> `wait()` releases the monitor.

This allows another thread to acquire the same monitor and modify the state required to make the condition true.

---

# 10. `notify()` and `notifyAll()`

Another thread can execute:

```java
synchronized (lock) {

    condition = true;

    lock.notifyAll();
}
```

Conceptually:

```text
waiting thread is notified
        ↓
it leaves the wait set
        ↓
it still has to reacquire the monitor
        ↓
after acquiring it, execution continues
```

Important:

> `notify()` does not immediately give the monitor to the waiting thread.

The notified thread still has to compete to reacquire the monitor.

---

# 11. Reentrancy

A lock is **reentrant** when a thread that already owns the lock can acquire the same lock again.

Java's intrinsic monitor used by `synchronized` is already reentrant.

Example:

```java
public synchronized void methodA() {

    methodB();
}

public synchronized void methodB() {

}
```

Both synchronized instance methods use:

```text
this object's monitor
```

Suppose T1 calls `methodA()`.

```text
T1 enters methodA
lock count = 1
```

Then `methodA()` calls `methodB()`.

Because T1 already owns the same monitor:

```text
T1 enters methodB
lock count = 2
```

When methodB completes:

```text
lock count = 1
```

When methodA completes:

```text
lock count = 0
monitor released
```

Definition:

> A thread that already owns a lock can acquire the same lock again without blocking itself.

---

# 12. Why Is Reentrancy Needed?

Without reentrancy:

```text
T1 acquires monitor in methodA
        ↓
methodA calls methodB
        ↓
methodB tries to acquire same monitor
        ↓
T1 would wait for itself
        ↓
deadlock
```

Reentrancy prevents this situation.

---

# 13. `synchronized` vs `ReentrantLock`

Do not think that reentrancy belongs only to `ReentrantLock`.

Both are reentrant.

## `synchronized`

```java
synchronized (lock) {

    // critical section
}
```

Uses:

```text
JVM intrinsic / monitor lock
```

Java automatically releases it when the block exits, including when an exception occurs.

---

## `ReentrantLock`

```java
Lock lock = new ReentrantLock();

lock.lock();

try {

    // critical section

} finally {

    lock.unlock();
}
```

This is an explicit locking API.

It gives additional features such as:

```text
tryLock()
timed tryLock()
lockInterruptibly()
fairness option
multiple Condition objects
```

---

# 14. Better Hierarchy

```text
Synchronization Mechanisms
        |
        ├── Intrinsic Locking
        │       |
        │       └── synchronized
        │                |
        │                └── Object Monitor
        │
        ├── Explicit Locking
        │       |
        │       ├── ReentrantLock
        │       ├── ReadWriteLock
        │       └── StampedLock
        │
        └── Atomic Operations
                |
                ├── AtomicInteger
                ├── AtomicLong
                └── AtomicReference
```

---

# 15. Atomic Operations

Example:

```java
AtomicInteger counter = new AtomicInteger();

counter.incrementAndGet();
```

For a simple counter, this can avoid:

```java
synchronized (lock) {

    counter++;
}
```

Atomic classes are useful when the operation can be represented as a single atomic state transition.

---

# 16. Atomic Does Not Mean Every Business Operation Is Safe

Consider:

```text
if balance >= amount
    balance = balance - amount
```

This involves:

```text
check
  +
decision
  +
update
```

Making `balance` an `AtomicInteger` does not automatically make the complete business operation safe.

You must protect the **whole invariant**, not just individual reads and writes.

---

# 17. Monitor Information — Correct Mental Model

Instead of saying:

> Monitor contains lock owner, lock count and waiting threads list.

Prefer:

> A Java monitor manages ownership of its intrinsic lock, reentrant acquisition, threads contending to acquire the monitor, and a wait set used by `wait()`, `notify()` and `notifyAll()`.

Conceptually:

```text
Monitor
   |
   ├── Current lock owner
   |
   ├── Reentrant acquisition count
   |
   ├── Threads competing for monitor
   |
   └── Wait set
          ↓
       threads that called wait()
```

---

# 18. Important Interview Distinction

### BLOCKED

A thread is trying to enter a synchronized section but another thread owns the monitor.

```text
T1 owns monitor

T2 tries to enter
↓
T2 = BLOCKED
```

### WAITING

A thread already acquired the monitor but deliberately called:

```java
wait();
```

Then:

```text
T1 calls wait()
↓
T1 releases monitor
↓
T1 enters wait set
```

These are two different situations.

---

# 19. One-Line Revision Statements

**Race condition**

> Multiple threads access shared mutable state and the result depends on execution timing/interleaving.

**Critical section**

> Code accessing shared mutable state that must be protected from unsafe concurrent execution.

**Synchronization**

> Coordination mechanism used to safely control concurrent access to shared mutable state.

**Monitor**

> Object-associated synchronization mechanism providing intrinsic locking and thread coordination.

**`synchronized`**

> Java language construct that acquires/releases an object's monitor and provides mutual exclusion plus visibility guarantees.

**Mutual exclusion**

> Only one thread can own the same monitor and execute its protected critical section at a time.

**Reentrancy**

> A thread that already owns a lock can acquire the same lock again.

**`wait()`**

> Releases the object's monitor and places the thread into that monitor's wait set.

**`notify()`**

> Wakes one waiting thread, which must still reacquire the monitor before continuing.

**`notifyAll()`**

> Wakes all threads in the monitor's wait set; they compete to reacquire the monitor.

**ReentrantLock**

> Explicit reentrant locking API with capabilities beyond `synchronized`.

**Atomic operation**

> An operation that behaves as one indivisible state transition with respect to competing threads.

---

# 20. Final Revision Flow

```text
Concurrency
    ↓
Multiple threads
    ↓
Shared mutable state
    ↓
Race condition
    ↓
Identify critical section
    ↓
Protect using synchronization
    ↓
Choose mechanism

    ├── synchronized
    │       ↓
    │   Object monitor
    │       ↓
    │   Mutual exclusion
    │   Visibility
    │   Reentrancy
    │   wait / notify / notifyAll
    │
    ├── ReentrantLock / other explicit locks
    │
    └── Atomic classes
            ↓
       simple atomic state transitions
```

---

# 21. What You Should Be Able to Explain Without Notes

After revising this topic, you should be able to answer:

1. Why does shared mutable state cause race conditions?
2. What exactly is a critical section?
3. What is an object monitor?
4. How does `synchronized` use a monitor?
5. What happens when T1 owns a monitor and T2 reaches the same synchronized block?
6. What is the difference between a blocked thread and a thread in the monitor wait set?
7. Why does `wait()` release the monitor?
8. Does `notify()` immediately transfer the lock?
9. What does reentrant mean?
10. Is `synchronized` reentrant?
11. Why would you choose `ReentrantLock` over `synchronized`?
12. When is an atomic class sufficient, and when is synchronization still required?
