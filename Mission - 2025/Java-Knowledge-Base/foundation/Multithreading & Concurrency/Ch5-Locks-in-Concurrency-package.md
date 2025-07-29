# 🔐 ReentrantLock in Java
### 28-07-2025,29-07-2025
`ReentrantLock` is a lock class available in the `java.util.concurrent.locks` package. It provides explicit locking mechanisms with advanced features over the traditional `synchronized` keyword.

## What does "Reentrant" mean?

A **reentrant** lock allows the **same thread** to acquire the lock multiple times **without blocking itself**. This is useful in nested locking scenarios.

Both `ReentrantLock` and `synchronized` are reentrant in Java.

---

## Differences Between `synchronized` and `ReentrantLock`

| Feature                          | `synchronized`                  | `ReentrantLock`                         |
|----------------------------------|----------------------------------|------------------------------------------|
| Reentrant                        | ✅ Yes                           | ✅ Yes                                   |
| Timeout for acquiring lock       | ❌ No                            | ✅ Yes (`tryLock(long, TimeUnit)`)       |
| Interruptible lock acquisition   | ❌ No                            | ✅ Yes (`lockInterruptibly()`)           |
| Fairness                         | ❌ No                            | ✅ Yes (via constructor)                 |
| Condition variables              | ❌ No                            | ✅ Yes (`newCondition()`)                |
| Explicit lock/unlock             | ❌ No (automatic by JVM)         | ✅ Yes (manual)                          |
| Monitor inspection (e.g., count) | ❌ No                            | ✅ Yes (`getHoldCount()`, etc.)          |

---

## When to Use `ReentrantLock`?

You should prefer `ReentrantLock` over `synchronized` when:
- You need **timeout** or **interruptible** lock attempts.
- You want to use **Condition variables** (like `wait/notify` but more flexible).
- You need to **inspect lock state** (e.g., who holds it, hold count).
- You want to enforce **fairness policy** for threads.

For simple mutual exclusion, `synchronized` is usually sufficient and easier to use.

---

## Code Example

Below is a simple example demonstrating reentrant behavior of `ReentrantLock`.

```java
package org.example.concurrency;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantExample {

    ReentrantLock reentrantLock = new ReentrantLock();

    void tryToOpenPasswordProtectedFile() {
        reentrantLock.lock(); // Acquiring first time
        try {
            System.out.println("Trying to open a file...");
            enterPasswordAndOpenFile(); // Nested lock attempt
        } finally {
            reentrantLock.unlock(); // Unlocking one level
        }
    }

    void enterPasswordAndOpenFile() {
        reentrantLock.lock(); // Acquiring second time (same thread)
        try {
            System.out.println("Entered password and opened the file");
        } finally {
            reentrantLock.unlock(); // Unlocking second level
        }
    }

    public static void main(String[] args) {
        ReentrantExample example = new ReentrantExample();
        example.tryToOpenPasswordProtectedFile();
    }
}
```

# 📘 ReentrantReadWriteLock in Java

`ReentrantReadWriteLock` is a lock that allows **multiple threads to read** a resource **concurrently** as long as **no thread is writing** to it. When a **write lock is held**, all reads and other writes are **blocked**.

It helps improve performance in scenarios where:
- Read operations are frequent.
- Write operations are infrequent.
- Consistency must still be ensured.

---

## Key Components

`ReentrantReadWriteLock` provides two distinct locks:
- 🔓 **Read Lock**: Shared – multiple threads can hold it simultaneously.
- 🔐 **Write Lock**: Exclusive – only one thread can hold it at a time.

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteExample {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    
    private int sharedResource = 0;

    public void readData() {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " reading: " + sharedResource);
            Thread.sleep(500); // simulate time-consuming read
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            readLock.unlock();
        }
    }

    public void writeData(int value) {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " writing: " + value);
            sharedResource = value;
            Thread.sleep(500); // simulate time-consuming write
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        ReadWriteExample example = new ReadWriteExample();

        Runnable readTask = example::readData;
        Runnable writeTask = () -> example.writeData(42);
        // Start multiple read threads
        for (int i = 0; i < 3; i++) {
            new Thread(readTask, "Reader-" + i).start();
        }

        // Start one write thread
        new Thread(writeTask, "Writer").start();
    }
}
```
### Reentrancy in ReentrantReadWriteLock

- A thread can acquire the read lock multiple times, if it already holds it.
-A thread cannot acquire the write lock if it already holds the read lock – doing so will lead to deadlock.
  -A thread can upgrade from write lock to read lock, but not vice versa (read → write is not allowed - read in Important notes section below).

### Fairness Policy
-Just like ReentrantLock, ReentrantReadWriteLock supports a fairness policy:
-Fair mode: Longest waiting thread gets access first.
-Unfair mode (default): May favor throughput but can cause starvation.

```java
ReentrantReadWriteLock fairLock = new ReentrantReadWriteLock(true); // true = fair
```

## When to Use
**Use ReentrantReadWriteLock when:**
-Read operations happen frequently.
-Write operations are rare but critical.
-You want to avoid full synchronization for better performance.
-Data consistency is still required in concurrent reads/writes.

## Important Notes
-Unlike synchronized, you must manually acquire and release both read and write locks.
-Improper use (like acquiring a read lock then trying to upgrade to a write lock) can lead to deadlocks.
-Read-write locks work best when reads dominate writes. If writes are frequent, use ReentrantLock or synchronized instead.

A thread can safely acquire a read lock after holding the write lock (write ➝ read), called downgrading.
However, upgrading from read lock to write lock is not allowed, as the thread will wait for all read locks (including its own) to be released, leading to deadlock.
To avoid this, always release the read lock before acquiring the write lock if you need to upgrade.

## Differences Between `synchronized` and `Lock` (ReentrantLock)

| Feature                        | `synchronized`                                 | `Lock` (e.g., `ReentrantLock`)                      |
|-------------------------------|-------------------------------------------------|-----------------------------------------------------|
| Basic Nature                  | Keyword (language-level construct)              | Class (from `java.util.concurrent.locks`)           |
| Lock Acquisition              | Implicit (automatically acquired & released)    | Explicit (requires `lock()` and `unlock()`)         |
| Flexibility                   | Limited                                         | High — supports interruptible, timed, and try locks |
| Interruptibility              | Cannot interrupt a waiting thread               | `lockInterruptibly()` allows interrupting threads   |
| Timeout Support               | Not available                                   | `tryLock(timeout)` available                        |
| Fairness                      | No support                                      | Can be set (`new ReentrantLock(true)`)              |
| Lock Downgrading              | Not supported                                   | Supported (write ➝ read in `ReentrantReadWriteLock`)|
| Condition Variables           | Not supported directly                          | Supports `Condition` for advanced waiting/notify    |
| Performance                   | Slightly lower overhead                         | May have better performance under contention        |
| Code Simplicity               | Easier and less error-prone                     | Requires careful `try-finally` for unlocking        |
| Reentrancy                    | Supported                                       | Supported                                            |

> ✅ Use `synchronized` for simple cases.  
> 🔧 Use `Lock` when you need timed waits, fairness, interruptibility, or complex coordination.

# StampedLock

StampedLock is designed for high-performance, read-heavy scenarios.
It introduces optimistic and pessimistic (read below) locking mechanisms to reduce contention(read below) and increase throughput.

Unlike ReentrantLock, StampedLock is not reentrant — this means:

- If a thread holding a lock (read/write) tries to acquire it again, it can lead to deadlock or blocking, because the lock system does not track ownership per thread.
- Therefore, lock acquisition must be handled carefully, especially in nested or recursive methods.

🔁 Reentrancy is intentionally avoided in StampedLock to improve performance and reduce overhead.
---
# Understand different locking mechanisms

## 🔄 Contention, Optimistic Lock, and Pessimistic Lock

## 🔁 Contention

**Contention** occurs when **multiple threads try to access the same shared resource** (like a variable, file, or database record) **at the same time**.

- High contention leads to performance bottlenecks due to frequent locking, waiting, or blocking.
- Example: Two threads trying to write to the same memory location simultaneously.

---

## 🔐 Pessimistic Locking

Pessimistic Locking assumes **conflicts will likely happen**, so it **locks the resource early** to avoid inconsistency.

- Thread **locks** the resource before reading or writing.
- Other threads **must wait** until the lock is released.
- Guarantees thread safety, but may reduce performance under low contention.

🧠 Example:  
Using `synchronized` or `ReentrantLock` — a thread locks the resource even if no one else is accessing it.

---

## 💡 Optimistic Locking

Optimistic Locking assumes **conflicts are rare**, so threads **access the resource without locking initially**.

- It **verifies later** if a conflict occurred.
- If there’s no conflict → proceed.
- If there’s a conflict → retry or rollback changes.

Commonly used in:
- `StampedLock.tryOptimisticRead()` in Java
- Versioning systems in databases or JPA

🧠 Example:  
A thread reads data without locking. Before writing, it checks if data has changed during the read. If so, it retries.

---

## 🔁 Summary Table

| Feature             | Pessimistic Locking       | Optimistic Locking        |
|--------------------|---------------------------|---------------------------|
| Assumes conflict?  | Yes                       | No                        |
| Locks resource?    | Before access             | After verifying conflict  |
| Performance        | Lower under low contention| Higher under low contention |
| Risk               | Thread blocking           | Retry on failure          |

