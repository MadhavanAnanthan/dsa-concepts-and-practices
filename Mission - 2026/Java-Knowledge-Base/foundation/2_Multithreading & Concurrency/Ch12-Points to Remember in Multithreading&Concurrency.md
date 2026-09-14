# Multithreading, Concurrency, and Parallelism in Java

## 🚀 Why Multithreading?

To achieve high performance and handle a large user base, using a single thread is not sufficient. That's why **multithreading** is introduced — it allows multiple tasks to run seemingly at the same time within a single process.

---

## 🔄 Threads and Processes

- A **process** is an instance of a running program, with its own memory space.
- A **thread** is the smallest unit of execution within a process.
- Each process has at least one thread (the **main thread**) and can spawn additional threads.
- Threads **share memory** within the process and can communicate with each other, unlike processes which are isolated.

---

## 🧵 Thread Basics in Java

- Java has supported multithreading since **Java 1.0**.
- Threads can be created using:
    - `Thread` class
    - `Runnable` interface
- Understanding the **thread lifecycle** (New, Runnable, Running, Blocked/Waiting, Terminated) is crucial.
- Managing thread safety, synchronization, and coordination manually is complex.

---

## 🔐 Thread Safety & Synchronization

- **Thread Safety** ensures shared data is accessed consistently in a multithreaded context.
- The `synchronized` keyword provides a **monitor lock** for mutual exclusion.
    - It can be applied to methods or blocks.
    - Ensures **only one thread** enters a critical section at a time.
- Use `join()` to make one thread wait for the completion of another.

---

## ⚠️ Common Problems in Multithreaded Systems

- **Race Condition** – Two or more threads access shared data without synchronization.
- **Deadlock** – Threads waiting on each other indefinitely.
- **Livelock** – Threads keep changing state in response to each other but cannot proceed.
- **Starvation** – A thread waits indefinitely to acquire resources due to other threads holding them.

---

## 🤖 Java Concurrency API

Java introduced the **java.util.concurrent** package to simplify multithreading.

### 🧰 Executors

Factory methods from the `Executors` class:
- `newFixedThreadPool(int n)`: Fixed number of threads.
- `newCachedThreadPool()`: Dynamically creates threads as needed.
- `newSingleThreadExecutor()`: One thread, executes tasks sequentially.
- `newScheduledThreadPool(int n)`: Executes tasks periodically or with delay.

---

## 🧠 Runnable vs Callable

- `Runnable`: No return value, cannot throw checked exceptions.
- `Callable<V>`:
    - Returns a value.
    - Can throw checked exceptions.
    - Submitted to an `ExecutorService`.
    - Result is retrieved via `Future`.

---

## 🔒 Advanced Locking Mechanisms

Located in `java.util.concurrent.locks`:

- `ReentrantLock`: Like `synchronized`, but more flexible.
- `ReentrantReadWriteLock`:
    - Allows multiple readers.
    - Only one writer at a time.
- `StampedLock`:
    - Optimistic and pessimistic read/write locks.
    - Higher performance for read-heavy scenarios.

---

## ⛓️ Thread Coordination Utilities

- `Semaphore`: Allows N threads to access a resource.
- `CountDownLatch`: Waits for N operations to complete before proceeding.
- `CyclicBarrier`: Multiple threads wait for each other to reach a common point.
- `BlockingQueue`:
    - Waits when the queue is full (producer) or empty (consumer).
    - Common implementations:
        - `ArrayBlockingQueue`
        - `LinkedBlockingQueue`

---

## 🔁 Concurrent Collections

Thread-safe versions of standard collections:
- `ConcurrentHashMap`: Highly scalable, non-blocking hash map.
- `CopyOnWriteArrayList`: Creates a copy of the list on every write. Efficient for read-heavy use cases.

---

## 🔬 Atomic Classes

Located in `java.util.concurrent.atomic`:

- Classes like `AtomicInteger`, `AtomicLong`, `AtomicReference` provide **atomic operations**.
- Use **CAS (Compare-And-Swap)** internally.
- Avoids explicit synchronization.

---

## 🧠 Volatile Keyword

- Guarantees **visibility** of changes to variables across threads.
- Does **not** ensure atomicity.
- Useful for flags and state variables shared between threads.

---

## 📊 Fairness & Priorities

- Thread scheduling and lock fairness can be controlled via constructors in `ReentrantLock`, `Semaphore`, etc.
- Fairness ensures **first-come-first-served** access, avoiding starvation.

---

## 🧮 Parallelism

- True parallelism requires **multi-core CPUs**.
- Concurrency = structure, Parallelism = speed.
- Java provides the `ForkJoinPool` for **divide-and-conquer parallelism**.
- Built on the **work-stealing** algorithm.

---

## 🛠️ Debugging Multithreaded Applications

- Debugging race conditions, deadlocks, and livelocks is challenging.
- Use of `final`, `immutable` objects, and effective final variables improves thread safety.
- Tools like VisualVM, JFR, or JConsole help monitor thread states and locks.

---

## 🎯 Real-world Use Cases

- **Producer-Consumer** pattern using `BlockingQueue`.
- **Reader-Writer** using `ReentrantReadWriteLock`.
- **Thread pools** for web servers or task execution.
- **Atomic counters**, task coordination, and more.

---

## ✅ Summary

Java provides a powerful set of tools to build multithreaded and concurrent applications:
- Start with `Thread` and `Runnable` for basics.
- Use the `ExecutorService`, `Callable`, and `Future` for managed concurrency.
- Apply advanced synchronization using locks, semaphores, and atomic variables.
- Prefer thread-safe collections and design for immutability when possible.

