# Java Memory Model (JMM) — Practical Notes

The **Java Memory Model (JMM)** defines the rules that govern how multiple Java threads interact through memory.

It is not a separate physical memory area like:

- Heap
- Stack
- Metaspace
- PC Register
- Native Method Stack

Instead, JMM defines the **legal behavior of reads, writes, visibility, ordering, and synchronization between threads**.

---

# 1. Where JMM Fits

A useful mental model is:

```text
Java Source Code
      │
      v
Java Compiler
      │
      v
Bytecode
      │
      v
JVM
├── Interpreter
├── JIT Compiler
└── Runtime
      │
      v
CPU
├── Registers
├── Store Buffers
├── CPU Caches
└── Main Memory
```

The JMM sits conceptually across this execution path:

```text
                 JMM RULES
        ┌─────────────────────────┐
        │ Visibility              │
        │ Ordering                │
        │ Atomicity               │
        │ Happens-Before          │
        │ Synchronization Rules   │
        └─────────────────────────┘
                    │
                    v
Java Compiler → JVM/JIT → CPU/Memory
```

JMM ensures that optimizations performed by:

- Java compiler
- JIT compiler
- CPU
- Memory subsystem

do not violate the guarantees defined by the Java language memory model.

The JVM may optimize aggressively, but observable behavior must still follow JMM rules.

---

# 2. Why JMM Exists

Modern CPUs and JVMs optimize aggressively for performance.

A thread may:

- Keep values in CPU registers
- Read from CPU cache
- Delay writes
- Reorder instructions
- Execute instructions out of apparent source-code order

Without a memory model, multithreaded programs could behave unpredictably across different CPUs and JVM implementations.

JMM provides a balance between:

```text
Correct Multithreaded Behavior
            +
High Performance
```

It does **not** force every read/write to go directly to physical main memory.

Instead, it defines when one thread is guaranteed to observe another thread's actions.

---

# 3. Visibility

Visibility means:

> When one thread changes a shared value, when is another thread guaranteed to see that change?

Example:

```java
class Task {

    boolean running = true;

    void stop() {
        running = false;
    }

    void execute() {
        while (running) {
            // work
        }
    }
}
```

Suppose:

```text
Thread-A → execute()
Thread-B → stop()
```

Thread-B writes:

```text
running = false
```

But Thread-A may continue seeing an older value because the JMM does not require unsynchronized reads and writes to become immediately visible.

Conceptually:

```text
Thread-A                 Thread-B

running = true           running = true
(local/cached view)      │
                         v
                    running = false
```

Without synchronization, Thread-A is not guaranteed to observe the update.

---

# 4. volatile

`volatile` provides a visibility and ordering guarantee for a field.

```java
volatile boolean running = true;
```

Now:

```java
void stop() {
    running = false;
}
```

and:

```java
while (running) {
}
```

have JMM-defined visibility semantics.

The important rule is:

> **A write to a volatile variable happens-before every subsequent read of that same volatile variable.**

Conceptually:

```text
Thread-B
running = false
     │
     │ volatile write
     v
visibility / ordering guarantee
     │
     v
Thread-A
volatile read → sees latest permitted value
```

## Important Clarification

It is common to say:

> volatile always reads directly from main memory.

That is an oversimplification.

Modern CPUs still use caches.

The correct idea is:

> **volatile gives JMM visibility and ordering guarantees. The JVM/JIT generates the necessary machine-level operations and barriers so threads observe the required behavior.**

## What volatile guarantees

`volatile` provides:

- Visibility
- Ordering constraints around volatile reads/writes

It does **not** make compound operations atomic.

Example:

```java
volatile int count = 0;

count++;
```

`count++` is conceptually:

```text
read count
add 1
write count
```

Multiple threads can still interfere.

---

# 5. Atomicity

Atomicity means:

> An operation behaves as one indivisible action.

Example:

```java
int x = 10;
```

A simple read/write of a reference or a primitive value up to 32 bits is atomic under the Java specification. Reads and writes of `volatile long` and `volatile double` are also atomic. The specification permits a non-volatile 64-bit `long` or `double` access to be split into two 32-bit accesses, even though modern JVM implementations commonly perform it atomically. Correctly synchronized code must not rely on an implementation accident.

But:

```java
count++;
```

is not one atomic read-modify-write operation.

It consists of multiple logical steps:

```text
1. Read count
2. Add 1
3. Write count
```

Example:

```text
Initial count = 10

Thread-A             Thread-B

read 10              read 10
+1                   +1
write 11             write 11
```

Expected:

```text
12
```

Actual:

```text
11
```

This is a **lost update**.

---

# 6. Race Condition

A race condition is the broad situation in which correctness depends on thread timing or interleaving. It commonly involves multiple threads performing a compound action on shared mutable state without sufficient coordination.

A **data race** is more specific: two conflicting accesses to the same variable, at least one a write, are not ordered by happens-before. Many race conditions contain data races, but the terms are not identical; a higher-level check-then-act race can also be built from individually thread-safe operations.

Example:

```java
class Counter {

    private int count = 0;

    void increment() {
        count++;
    }
}
```

Two threads:

```text
Thread-A              Thread-B

read 0                read 0
increment             increment
write 1               write 1
```

Final result:

```text
1
```

instead of:

```text
2
```

Race conditions are about **incorrect outcomes caused by unsafe interleavings**.

---

# 7. synchronized

`synchronized` provides mutual exclusion and memory-visibility guarantees.

Example:

```java
public synchronized void increment() {
    count++;
}
```

Only one thread at a time can execute the synchronized section for the same monitor.

Conceptually:

```text
Thread-A
    │
    v
Acquire Monitor
    │
    v
Critical Section
    │
    v
Release Monitor
    │
    v
Thread-B can acquire
```

## synchronized provides two important properties

### 1. Mutual Exclusion

Only one thread owns the monitor at a time.

### 2. Visibility

A monitor unlock happens-before a subsequent lock of the same monitor.

```text
Thread-A
writes shared data
      │
      v
unlock monitor
      │
      │ happens-before
      v
lock same monitor
      │
      v
Thread-B sees protected updates
```

## Performance

`synchronized` is lock-based, so contention can reduce performance.

However:

> Do not assume synchronized is always slow.

Modern JVMs optimize monitor operations heavily.

Performance becomes a concern mainly when there is:

- High contention
- Long critical sections
- Too many threads competing for the same monitor

## Monitor and synchronized implementation boundary

Every Java object can participate in monitor synchronization. A synchronized block compiles to bytecode using `monitorenter` and `monitorexit` with exception-safe release behavior. A synchronized method is marked with the `ACC_SYNCHRONIZED` method flag; its synchronization is not expressed by an ordinary explicit `monitorenter` instruction in that method's bytecode.

Conceptually, a monitor coordinates:

```text
monitor
├── current owner, if any
├── threads contending to acquire it
└── threads in the wait set after Object.wait()
```

`Object.wait()` requires ownership and releases the monitor while waiting. `notify()` or `notifyAll()` requires ownership and moves eligible waiter(s) toward reacquisition, but the notifying thread retains the monitor until it exits the synchronized region.

The Java/JMM guarantee is monitor ownership plus synchronization ordering. Do not treat historical biased/thin/heavy-lock diagrams or one fixed object-header bit layout as permanent Java rules; HotSpot locking and object-header implementations evolve across JDK versions.

---

# 8. Happens-Before

**Happens-before** is one of the most important JMM concepts.

If:

```text
Action A happens-before Action B
```

then:

1. Effects of A are visible to B
2. A is ordered before B according to JMM guarantees

It does not necessarily mean A literally executed earlier in wall-clock time in every internal hardware sense.

It means Java guarantees the required observable ordering.

## Important Happens-Before Rules

### Program Order Rule

Within one thread:

```java
int a = 10;
int b = a + 5;
```

Earlier actions happen-before later actions according to program-order semantics.

### Monitor Lock Rule

```text
unlock monitor
      happens-before
subsequent lock of same monitor
```

### volatile Rule

```text
volatile write
      happens-before
subsequent volatile read
```

of the same variable.

### Thread Start Rule

Actions before:

```java
thread.start();
```

happen-before actions performed by the started thread.

### Thread Join Rule

All actions in a thread happen-before another thread successfully returns from:

```java
thread.join();
```

### Transitivity

If:

```text
A happens-before B
B happens-before C
```

then:

```text
A happens-before C
```

---

# 9. Instruction Reordering

Compilers and CPUs may reorder operations to improve performance.

Example source:

```java
a = 10;
b = 20;
```

Internally, execution might be rearranged if doing so does not change the behavior of a correctly synchronized single-threaded program.

JMM permits optimizations as long as they do not violate required semantics.

## Why reordering matters

Consider:

```java
data = 42;
ready = true;
```

Another thread:

```java
if (ready) {
    System.out.println(data);
}
```

Without synchronization, another thread is not guaranteed to observe:

```text
data = 42
before
ready = true
```

in the way the programmer expects.

Correct version:

```java
int data;
volatile boolean ready;

void writer() {
    data = 42;
    ready = true;
}

void reader() {
    if (ready) {
        System.out.println(data);
    }
}
```

Because:

```text
data = 42
    │
    v
volatile write ready = true
    │
    │ happens-before
    v
volatile read ready
    │
    v
read data
```

The volatile relationship safely publishes the earlier write.

---

# 10. Memory Barriers / Memory Fences

Memory barriers are low-level mechanisms used by JVM/JIT-generated machine code to enforce required ordering and visibility constraints.

Conceptually:

```text
Normal Operations
       │
       v
Memory Barrier
       │
       v
Operations after barrier
```

The barrier restricts certain reorderings.

Different processor architectures may require different machine instructions.

The JMM defines the Java-level guarantee.

The JVM maps those guarantees onto the appropriate hardware mechanisms.

```text
JMM Semantic Requirement
          │
          v
JIT Compiler
          │
          v
Memory Barrier / CPU Instruction
          │
          v
Hardware Memory System
```

You normally do not manually insert CPU memory barriers in ordinary Java code.

Java constructs such as:

- `volatile`
- `synchronized`
- Atomic classes
- Locks

cause the JVM to provide the required memory-ordering behavior.

---

# 11. Compare-And-Set (CAS)

CAS is an atomic read-modify-write operation supported by modern processors.

Conceptually:

```text
CAS(memoryLocation, expectedValue, newValue)
```

Meaning:

```text
if currentValue == expectedValue
    replace it with newValue atomically
else
    fail
```

Example:

```text
Current value = 10

CAS(
    expected = 10,
    newValue = 11
)
```

Result:

```text
success
value = 11
```

If another thread already changed it:

```text
Current value = 12

CAS(
    expected = 10,
    newValue = 11
)
```

Result:

```text
failure
```

The caller can retry.

---

# 12. CAS Loop

Atomic classes commonly use retry-style logic.

Conceptually:

```java
while (true) {

    int current = get();

    int next = current + 1;

    if (compareAndSet(current, next)) {
        break;
    }
}
```

Two threads:

```text
Initial = 10

Thread-A                    Thread-B

read 10                     read 10
CAS 10 → 11 success         CAS 10 → 11 fails
                            │
                            v
                            read 11
                            CAS 11 → 12 success
```

Final value:

```text
12
```

No explicit heavyweight monitor lock is required for the counter update.

---

# 13. Atomic Classes

Java provides atomic classes such as:

```java
AtomicInteger
AtomicLong
AtomicBoolean
AtomicReference
```

Example:

```java
AtomicInteger counter = new AtomicInteger();

counter.incrementAndGet();
```

This performs an atomic update.

## Internal Mental Model

Modern Java implementations use JVM intrinsics / low-level runtime mechanisms such as `VarHandle` and hardware-supported atomic instructions.

Conceptually:

```text
AtomicInteger.incrementAndGet()
          │
          v
JVM / JIT intrinsic
          │
          v
Atomic CPU instruction / CAS-like operation
          │
          v
Hardware
```

Do not think of every atomic call as necessarily making a normal JNI/native-library function call.

The JIT can intrinsify these operations directly into efficient machine instructions.

## volatile inside Atomic Classes

Atomic classes maintain visibility semantics in addition to atomic update semantics.

A simplified conceptual model is:

```text
Atomic Variable
│
├── Visibility semantics
└── Atomic read-modify-write operations
```

Historically and internally, atomic field state is associated with volatile-style memory semantics.

The key difference is:

```text
volatile
    → visibility + ordering
    → compound update still not atomic

AtomicInteger
    → visibility + ordering
    → atomic read-modify-write operations
```

Example:

```java
volatile int count;
count++;
```

is unsafe for concurrent increments.

But:

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();
```

is atomic.

## VarHandle access modes

`VarHandle` provides explicit access modes with different atomicity and ordering strengths:

```text
plain
opaque
acquire / release
volatile
atomic compare-and-set / exchange / update
```

Plain access imposes no inter-thread ordering beyond ordinary JMM rules. Acquire/release modes provide directional ordering, while volatile modes provide volatile memory semantics. Atomic update modes combine an atomic operation with their documented ordering. Mixing access modes to the same variable requires care; a stronger declaration such as `volatile` does not automatically strengthen a VarHandle call made with a weaker explicit mode.

---

# 14. Lock-Based vs Lock-Free Style

## Lock-Based

Example:

```java
synchronized void increment() {
    count++;
}
```

Conceptually:

```text
Acquire Lock
     │
     v
Update
     │
     v
Release Lock
```

## CAS / Lock-Free Style

Conceptually:

```text
Read
  │
  v
Compute
  │
  v
CAS
 ├── success → done
 └── fail    → retry
```

CAS avoids exclusive monitor ownership for simple updates.

However:

> Lock-free does not automatically mean faster in every situation.

Under very high contention, many failed CAS retries can also be expensive.

---

# 15. ABA Problem

CAS compares values.

Consider:

```text
Initial value = A

Thread-1 reads A

Thread-2:
A → B → A

Thread-1 performs CAS:
expected = A
current  = A
```

CAS sees the expected value again and may succeed, even though the value changed in between.

This is called the **ABA problem**.

Java provides tools such as:

```java
AtomicStampedReference
AtomicMarkableReference
```

for scenarios where version/change information must also be tracked.

---

# 16. ConcurrentHashMap

`ConcurrentHashMap` is designed for safe concurrent access with much better concurrency than synchronizing every operation on one global lock.

Example:

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();
```

Multiple threads can safely perform operations such as:

```java
map.get("A");
map.put("A", 10);
map.compute("A", ...);
```

## High-Level Internal Idea

Modern `ConcurrentHashMap` uses a combination of techniques such as:

- volatile reads/writes
- CAS
- fine-grained synchronization
- bin-level locking when required
- specialized handling during resize

Conceptually:

```text
ConcurrentHashMap
│
├── Reads
│   └── mostly non-blocking
│
├── Simple structural updates
│   └── CAS where possible
│
└── Contended / complex updates
    └── localized locking
```

It does **not** lock the entire map for every operation.

## Why it performs better than a single synchronized HashMap wrapper

Global locking:

```text
Thread-A ─┐
Thread-B ─┼──→ One Global Lock → Entire Map
Thread-C ─┘
```

ConcurrentHashMap:

```text
Thread-A → bucket/bin X
Thread-B → bucket/bin Y
Thread-C → bucket/bin Z
```

When operations affect different internal locations, they can often proceed concurrently.

---

# 17. Safe Publication and Double-Checked Locking

**Safe publication** means making an object reference available through a mechanism that guarantees another thread observes the required initialized state. Common mechanisms include:

- Completing class initialization for an object stored in a static initializer.
- Publishing through a volatile write followed by the corresponding volatile read.
- Publishing while holding a monitor/lock and reading after the corresponding lock acquisition.
- Passing the object through a concurrency utility with documented happens-before guarantees.
- Establishing the appropriate ordering through thread start/join rules.

Merely assigning an object to an ordinary shared field is not safe publication when threads access that field without a happens-before relationship.

## Final-field semantics

Final fields have special initialization-safety rules. If an object is properly constructed and its reference does not escape during construction, a thread that later obtains the reference receives the JMM's final-field visibility guarantee for values assigned in the constructor. This limited guarantee does not make the whole object immutable, safely publish later mutations, or repair a constructor that leaks `this`.

Prefer immutable design with final fields plus a normal safe-publication mechanism; it makes the guarantee clear for both final and non-final reachable state.

## Double-checked locking

Double-checked locking is often used for lazy singleton initialization.

Incorrect historical-style idea without visibility guarantee:

```java
private static Singleton instance;

public static Singleton getInstance() {

    if (instance == null) {

        synchronized (Singleton.class) {

            if (instance == null) {
                instance = new Singleton();
            }
        }
    }

    return instance;
}
```

The issue is that object construction and reference publication involve multiple effects.

Conceptually:

```text
1. Allocate memory
2. Initialize object
3. Publish reference
```

Without the proper ordering guarantee, another thread could theoretically observe a published reference without observing complete initialization as required.

Correct form:

```java
private static volatile Singleton instance;

public static Singleton getInstance() {

    if (instance == null) {

        synchronized (Singleton.class) {

            if (instance == null) {
                instance = new Singleton();
            }
        }
    }

    return instance;
}
```

`volatile` safely publishes the instance.

Flow:

```text
First Check
instance == null?
      │
      ├── No → return
      │
      └── Yes
            │
            v
      synchronized block
            │
            v
       Second Check
            │
            v
      Create Instance
            │
            v
      volatile publication
```

Why two checks?

```text
First check
    → avoids locking after initialization

Second check
    → prevents multiple initialization while threads contend
```

---

# 18. Visibility vs Atomicity vs Ordering

These three concepts should be kept separate.

## Visibility

Question:

```text
Will Thread-B see Thread-A's write?
```

Common tools:

```text
volatile
synchronized
locks
atomics
```

## Atomicity

Question:

```text
Can another thread interfere halfway through this operation?
```

Common tools:

```text
synchronized
Lock
AtomicInteger
CAS
```

## Ordering

Question:

```text
Can operations appear reordered to another thread?
```

Controlled through:

```text
happens-before
volatile
synchronized
locks
atomic operations
memory barriers
```

Summary:

| Concept | Main Question | Common Mechanism |
|---|---|---|
| Visibility | Can another thread see the update? | volatile, locks, synchronized, atomics |
| Atomicity | Can the operation be interrupted/interleaved? | synchronized, locks, CAS, atomics |
| Ordering | Can operations be observed out of intended order? | happens-before, volatile, barriers |
| Mutual exclusion | Can only one thread enter? | synchronized, Lock |

---

# 19. Race Condition vs Data Race

These terms are related but not identical.

A **data race** occurs when conflicting memory accesses happen concurrently without the required happens-before ordering.

A **race condition** is broader:

> Program correctness depends on thread timing/interleaving.

Example:

```java
if (balance >= amount) {
    balance -= amount;
}
```

Two threads may both pass the condition and both withdraw.

Even if individual reads/writes are atomic, the complete check-then-act sequence is not atomic.

---

# 20. Practical JMM Flow

For shared state:

```java
volatile boolean ready;
int data;
```

Writer:

```java
data = 100;
ready = true;
```

Reader:

```java
if (ready) {
    System.out.println(data);
}
```

Flow:

```text
Thread-A
data = 100
     │
     v
volatile write ready = true
     │
     │ happens-before
     v
volatile read ready
     │
     v
Thread-B
read data
```

Because of JMM volatile semantics, when Thread-B observes the appropriate volatile write, the earlier write to `data` is also visible.

---

# 21. JMM Complete Mental Model

```text
                         JAVA MEMORY MODEL
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
          v                     v                     v
      Visibility            Atomicity              Ordering
          │                     │                     │
          v                     v                     v
       volatile              CAS                happens-before
   synchronized          Atomic Classes         memory barriers
     atomics              synchronized            volatile
          │                     │                     │
          └─────────────────────┼─────────────────────┘
                                │
                                v
                         Safe Concurrency
```

Then underneath:

```text
Java Code
   │
   v
JMM Guarantees
   │
   v
Compiler / JIT
   │
   v
Memory Barriers / Atomic Instructions
   │
   v
CPU Caches / Registers / Main Memory
```

---

# 22. Important Relationship Between Concepts

```text
Shared Mutable State
        │
        v
Possible Race Condition
        │
        ├── Need Visibility?
        │       └── volatile / synchronization
        │
        ├── Need Atomic Read-Modify-Write?
        │       ├── synchronized / Lock
        │       └── CAS / Atomic classes
        │
        ├── Need Ordering?
        │       └── happens-before / barriers
        │
        └── Need Concurrent Data Structure?
                └── ConcurrentHashMap etc.
```

---

# 23. What to Use When

## Only need visibility of a simple state flag

Use:

```java
volatile
```

Example:

```java
volatile boolean shutdown;
```

## Need atomic counter update

Use:

```java
AtomicInteger
```

Example:

```java
counter.incrementAndGet();
```

## Need multiple operations to execute as one critical section

Use:

```java
synchronized
```

or:

```java
Lock
```

Example:

```java
synchronized (account) {
    checkBalance();
    withdraw();
    updateLedger();
}
```

## Need a thread-safe shared map

Use:

```java
ConcurrentHashMap
```

rather than manually synchronizing a normal `HashMap` in most concurrent-use cases.

---

# 24. Final End-to-End View

```text
Multiple Threads
      │
      v
Shared Heap Objects
      │
      v
Potential Problems
├── Visibility
├── Race Conditions
├── Atomicity
└── Reordering
      │
      v
JMM Defines Rules
├── Happens-Before
├── volatile semantics
├── monitor semantics
└── ordering guarantees
      │
      v
Java Concurrency Mechanisms
├── volatile
├── synchronized
├── Lock
├── AtomicInteger / AtomicReference
├── CAS
└── ConcurrentHashMap
      │
      v
JVM / JIT Implementation
├── memory barriers
├── compiler constraints
├── JVM intrinsics
└── atomic machine instructions
      │
      v
CPU / Hardware
├── registers
├── caches
├── cache coherence
├── atomic instructions
└── memory ordering
```

---

# 25. One-Line Memory Model

> **JMM defines the rules; `volatile`, `synchronized`, locks and atomic classes provide Java-level mechanisms; the JVM/JIT implements those guarantees using compiler restrictions, memory barriers and hardware atomic instructions.**

---

# Authoritative References

- [Java Language Specification, Chapter 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se25/html/jls-17.html)
- [`VarHandle` API: plain, opaque, acquire/release, volatile, and atomic access modes](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/VarHandle.html)
- [`java.util.concurrent` package specification and memory-consistency effects](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/package-summary.html)
