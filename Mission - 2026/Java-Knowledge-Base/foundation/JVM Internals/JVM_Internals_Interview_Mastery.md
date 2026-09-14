# 1. Complete JVM Architecture

The JVM can be understood through five major ideas:

1. **Class-file / bytecode format**
2. **Class loading and linking**
3. **Runtime memory areas**
4. **Execution and JIT compilation**
5. **Memory management / garbage collection**

```text
+--------------------------+
|      Java Source         |
|        .java             |
+------------+-------------+
             |
             | javac
             v
+--------------------------+
|     Java Bytecode        |
|        .class            |
+------------+-------------+
             |
             v
+--------------------------+
| Class Loading Subsystem  |
| Loading → Linking → Init  |
+------------+-------------+
             |
             v
+------------------------------------------------------+
|                 JVM Runtime                          |
|                                                      |
|  Shared                          Per Thread           |
|  +----------------+             +----------------+   |
|  | Heap           |             | Java Stack     |   |
|  +----------------+             +----------------+   |
|  | Method Area /  |             | PC Register    |   |
|  | Metaspace impl.|             +----------------+   |
|  +----------------+             | Native Stack   |   |
|                                 +----------------+   |
+--------------------------+---------------------------+
                           |
                           v
                +----------------------+
                |   Execution Engine   |
                | Interpreter + JIT    |
                +----------+-----------+
                           |
                           v
                    Native Machine Code
```

---

# 2. Bytecode and `.class` File Fundamentals

## Why Bytecode Exists

Java source is compiled into an intermediate instruction set understood by the JVM.

```text
Windows JVM ─┐
Linux JVM   ─┼── execute the same valid bytecode
macOS JVM   ─┘
```

This is the core of Java's platform independence.

## Inspecting Bytecode

```bash
javac Calculator.java
javap -c Calculator
```

Example:

```java
public int add(int a, int b) {
    return a + b;
}
```

Conceptual bytecode:

```text
iload_1
iload_2
iadd
ireturn
```

The JVM is largely **stack based**. Instructions commonly push operands onto an operand stack and consume them from there.

## Important `.class` File Contents

You do not need to memorize the binary format, but know the major components:

```text
.class
├── Magic number
├── Class-file version
├── Constant Pool
├── Access flags
├── This class / Super class
├── Interfaces
├── Fields
├── Methods
└── Attributes
```

### Constant Pool

Contains symbolic information used by bytecode, such as:

- class names
- method names
- field names
- string constants
- symbolic method/field references

```text
Bytecode
   |
   | symbolic reference
   v
Constant Pool
   |
   | resolution
   v
Runtime JVM representation
```

### Interview Question: Why Is Java Bytecode Stack-Based?

A stack-based instruction set is portable and does not expose a specific CPU register architecture. The JIT later converts frequently executed bytecode into optimized machine instructions for the actual CPU.

---

# 3. Class Loading Subsystem

## Lifecycle

```text
              CLASS LIFE CYCLE

.class
  |
  v
+---------+
| Loading |
+----+----+
     |
     v
+-----------------------------+
| Linking                     |
|  1. Verification            |
|  2. Preparation             |
|  3. Resolution              |
+-------------+---------------+
              |
              v
+----------------+
| Initialization |
+-------+--------+
        |
        v
      Usable
```

## Loading

The JVM:

1. Finds the binary representation of a class.
2. Creates the JVM's internal class representation.
3. Creates the associated `java.lang.Class` object.

## Built-In Class Loaders

```text
                 Bootstrap
                    ↑
                 Platform
                    ↑
                Application
                    ↑
              Custom Loaders
```

### Bootstrap ClassLoader

Loads fundamental runtime classes/modules.

In Java code:

```java
System.out.println(String.class.getClassLoader());
```

commonly prints:

```text
null
```

because the bootstrap loader is implemented natively and is represented as `null` through this API.

### Platform ClassLoader

Loads platform classes/modules not loaded by Bootstrap.

### Application ClassLoader

Normally loads:

- application classes
- dependencies on the class path/module path

## Parent Delegation

When a class loader receives a request:

```text
Application Loader receives request
             |
             v
      Ask parent first
             |
             v
       Platform Loader
             |
             v
       Bootstrap Loader
             |
      found? / not found?
             |
             v
Parent returns class OR child attempts loading
```

### Why Parent Delegation?

- prevents accidental duplicate definitions of core classes
- improves consistency
- contributes to JVM security boundaries

## Class Identity

A very important interview concept:

```text
Class Identity =
Fully Qualified Class Name
          +
Defining ClassLoader
```

Therefore two class loaders can load byte-identical:

```text
com.example.User
```

and the JVM can treat them as **different types**.

This explains many plugin/application-server class-cast problems.

## Linking

### Verification

Checks structural and bytecode safety rules before execution.

### Preparation

Creates storage required for class-level data and assigns default values.

```java
static int count = 10;
```

Conceptually during preparation:

```text
count = 0
```

Initialization later executes the initializer that results in `10`.

### Resolution

Symbolic references are converted into runtime references.

```text
CONSTANT_Methodref
        ↓
resolved runtime method reference
```

Resolution may be lazy.

## Initialization

Executes class initialization logic, including relevant static field initializers and static blocks.

```java
static int count = loadCount();

static {
    System.out.println("initialized");
}
```

The JVM represents class initialization using the special `<clinit>` method when one is required.

## When Does Initialization Happen?

Common active uses include:

- `new`
- invoking a static method
- accessing a non-constant static field
- reflective initialization
- initializing the startup class containing `main`

Important nuance:

```java
class Parent {
    static final int X = 10;
}
```

A compile-time constant may be inlined into the caller, so reading it does not necessarily initialize `Parent`.

## Custom ClassLoader

Basic pattern:

```java
public class MyClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name)
            throws ClassNotFoundException {

        byte[] bytes = loadBytes(name);
        return defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] loadBytes(String name) {
        throw new UnsupportedOperationException();
    }
}
```

Know the distinction:

```text
loadClass() → normally performs delegation
findClass() → subclass loading implementation
defineClass() → turns class bytes into a Class
```

---

# 4. JVM Runtime Data Areas

```text
JVM PROCESS
│
├── Shared
│   ├── Heap
│   └── Method Area
│       └── HotSpot: class metadata primarily in Metaspace
│
└── Per Thread
    ├── Java Stack
    ├── PC Register
    └── Native Method Stack
```

## Heap

Conceptually stores objects and arrays managed by the GC.

```java
Employee e = new Employee();
```

Conceptually:

```text
Thread Stack                         Heap
+------------------+             +------------------+
| e (reference) ---+------------>| Employee object  |
+------------------+             +------------------+
```

But remember: JIT optimizations may eliminate some physical allocations.

## Java Stack

Each Java thread has its own stack.

```text
Thread A                 Thread B
+-----------+            +-----------+
| Frame A3  |            | Frame B2  |
+-----------+            +-----------+
| Frame A2  |            | Frame B1  |
+-----------+            +-----------+
| Frame A1  |            +-----------+
+-----------+
```

`-Xss` controls thread stack size.

Example:

```bash
java -Xss1m Main
```

## PC Register

Each JVM thread has a program counter that tracks the current JVM instruction for non-native execution.

## Native Method Stack

Supports execution of native methods, typically through JNI/native runtime integration.

## Method Area

Specification-level shared runtime area containing per-class structures such as:

- runtime constant pool
- field/method information
- method code
- class/interface metadata

## Metaspace

HotSpot introduced Metaspace in Java 8 to replace PermGen for class metadata.

```text
Heap                           Native Memory
+--------------------+         +----------------------+
| Java Objects       |         | Metaspace            |
| Arrays             |         | Class Metadata       |
| Class mirrors etc. |         +----------------------+
+--------------------+
```

Useful option:

```bash
-XX:MaxMetaspaceSize=512m
```

Without an explicit maximum, Metaspace can grow according to native-memory availability and JVM ergonomics.

---

# 5. Stack Frame Internals

Every Java method invocation creates a new frame for that thread.

```text
Java Stack
+-----------------------------+
| methodC() frame             | ← current
|  Local Variable Array       |
|  Operand Stack              |
|  Frame/constant-pool info   |
+-----------------------------+
| methodB() frame             |
+-----------------------------+
| methodA() frame             |
+-----------------------------+
```

## Local Variable Array

Contains method parameters and local values.

For instance methods, local slot `0` normally contains:

```text
this
```

## Operand Stack

The JVM uses it for intermediate computation.

```java
int result = a + b;
```

Conceptually:

```text
load a
   ↓
+-----+
|  a  |
+-----+

load b
   ↓
+-----+
|  b  |
+-----+
|  a  |
+-----+

iadd
   ↓
+-----+
| a+b |
+-----+
```

## Dynamic Linking

A frame contains information enabling symbolic method references to be linked/resolved to runtime methods.

## Method Return

When the method completes:

```text
callee frame removed
       ↓
result passed to caller if required
       ↓
caller becomes current frame
```

## StackOverflowError

Example:

```java
public static void recurse() {
    recurse();
}
```

```text
recurse()
  ↓
recurse()
  ↓
recurse()
  ↓
...
  ↓
Thread stack exhausted
  ↓
StackOverflowError
```

---

# 6. Object Memory Layout — HotSpot View

This section is implementation-specific rather than guaranteed by the Java language specification.

Typical HotSpot object:

```text
+--------------------------+
| Mark Word                |
+--------------------------+
| Klass Pointer            |
+--------------------------+
| Instance Fields          |
+--------------------------+
| Alignment Padding        |
+--------------------------+
```

## Mark Word

Contains runtime metadata whose exact layout depends on JVM/version/state, potentially involving:

- identity hash information
- GC-related age/state bits
- locking-related state

Do not memorize a single bit layout across all JVM versions.

## Klass Pointer

Allows HotSpot to associate an object with its class metadata.

## Instance Data

Contains the object's instance fields.

## Padding

HotSpot aligns objects according to JVM alignment requirements, commonly 8-byte alignment by default.

## Compressed OOPs

On suitable heap configurations, HotSpot can represent ordinary object pointers in a compressed form.

Common flags to inspect:

```bash
java -XX:+PrintFlagsFinal -version
```

Look for concepts such as:

```text
UseCompressedOops
UseCompressedClassPointers
```

Why?

```text
Smaller references
      ↓
Smaller object footprint
      ↓
Potentially better cache utilization
```

---

# 7. Object Allocation Internals

A common interview question:

> Is `new` expensive?

Not necessarily.

## Simplified Allocation Flow

```text
new Object()
     |
     v
Can JIT eliminate allocation?
     |
     +-- Yes → scalar replacement / optimization
     |
     No
     |
     v
Allocate from thread-local allocation region if possible
     |
     v
Initialize object
     |
     v
Return reference
```

## TLAB — Thread Local Allocation Buffer

HotSpot commonly gives application threads a small allocation region inside the heap.

```text
Heap / Young Allocation Area
+---------------------------------------+
| Thread A TLAB | Thread B TLAB | ...   |
+---------------------------------------+
```

Each thread can usually allocate by advancing a pointer.

```text
Before:
| used objects | free free free |
               ^ pointer

After allocation:
| used objects | NEW | free free |
                     ^ pointer
```

This is called **bump-pointer allocation**.

It avoids global locking for most normal allocations.

---

# 8. Execution Engine

```text
                    Bytecode
                       |
                       v
               +---------------+
               | Interpreter   |
               +-------+-------+
                       |
             profiling / hotness
                       |
                       v
               +---------------+
               | JIT Compiler  |
               +-------+-------+
                       |
                       v
                Native Machine
                     Code
```

## Interpreter

Advantages:

- starts executing quickly
- no need to wait for compilation
- gathers runtime profiling information

Disadvantage:

- repeatedly interpreting hot instructions is slower than optimized native code

## HotSpot

"HotSpot" refers to the JVM implementation used by OpenJDK/Oracle JDK and the idea of identifying frequently executed code paths ("hot spots") and optimizing them.

---

# 9. JIT Compilation

Modern HotSpot uses **tiered compilation**.

A useful conceptual model:

```text
Bytecode
   |
   v
Interpreter
   |
   | execution profiling
   v
C1 compilation
   |
   | hotter / richer profile
   v
C2 optimized compilation
```

The real tier system contains multiple compilation levels, but the above model is enough for most interviews.

## C1

Optimizes relatively quickly and helps application warm-up.

## C2

Performs more expensive optimizations intended for hot code.

## Code Cache

Compiled native code is stored in JVM-managed **Code Cache** memory.

```text
Bytecode
   |
   | JIT
   v
+--------------------+
| Native Code Cache  |
+--------------------+
```

## Important JIT Optimizations

### Method Inlining

```java
int result = add(10, 20);
```

Instead of retaining the call boundary, the JIT may effectively optimize toward:

```java
int result = 10 + 20;
```

Inlining also unlocks further optimizations.

### Escape Analysis

JIT analyzes whether an object's reference escapes:

- a method
- a thread
- a compilation scope

Example:

```java
public int calculate() {
    Point p = new Point(10, 20);
    return p.x + p.y;
}
```

If `p` does not escape, the JIT may optimize the allocation.

### Scalar Replacement

Instead of materializing:

```text
Point{x=10,y=20}
```

the JIT can sometimes operate directly on:

```text
x = 10
y = 20
```

The important conclusion is **allocation elimination**, not the oversimplified statement "escape analysis puts objects on the stack."

### Dead Code Elimination

Code proven to have no observable effect can be removed.

### Loop Optimizations

Hot loops can receive optimizations such as:

- loop unrolling
- invariant-code motion
- range-check elimination

## Deoptimization

JIT optimizations can rely on runtime assumptions.

```text
Profile says assumption is true
             ↓
JIT aggressively optimizes
             ↓
Assumption later becomes invalid
             ↓
Deoptimization
             ↓
Execution returns to less optimized/interpreted form
```

This ability lets HotSpot perform speculative optimizations safely.

---

# 10. Java Memory Model (JMM)

This is one of the most important advanced JVM/interview topics.

The JMM defines the legal interaction of threads with memory and provides rules for:

1. **Visibility**
2. **Ordering**
3. **Atomicity**

Do not confuse:

```text
JVM Runtime Memory Areas
```

with:

```text
Java Memory Model (JMM)
```

They are different concepts.

## Visibility Problem

```java
class Example {
    boolean running = true;

    void stop() {
        running = false;
    }

    void work() {
        while (running) {
        }
    }
}
```

If different threads execute `stop()` and `work()`, the JMM does not give the required visibility guarantee for this data race.

## `volatile`

```java
volatile boolean running = true;
```

A volatile write happens-before a subsequent volatile read of the same variable.

This gives important visibility and ordering guarantees.

### What `volatile` Does NOT Do

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

Multiple operations means the increment as a whole is not made atomic merely by `volatile`.

## `synchronized`

```java
synchronized (lock) {
    count++;
}
```

Provides:

- mutual exclusion for the same monitor
- happens-before visibility guarantees between monitor unlock and subsequent lock

Conceptually:

```text
Thread A                        Thread B
lock
  |
modify state
  |
unlock -----------------------> lock
          happens-before          |
                                  sees prior state
```

## Happens-Before

If operation A happens-before B, effects of A are guaranteed to be visible to B according to the JMM.

Essential rules to know:

### Program Order

Within a thread:

```text
A
↓
B
```

A happens-before B according to program order.

### Monitor Rule

```text
unlock(monitor)
       ↓ happens-before
later lock(monitor)
```

### Volatile Rule

```text
volatile write
       ↓ happens-before
subsequent volatile read
```

### Thread Start

Actions before:

```java
thread.start();
```

happen-before actions in the started thread.

### Thread Join

Actions performed by a thread happen-before another thread successfully returns from:

```java
thread.join();
```

## Reordering

Compiler, JIT and CPU may reorder operations when doing so preserves allowed single-thread semantics.

Synchronization constructs establish ordering constraints where cross-thread correctness requires them.

## Atomicity

Simple reads/writes have defined atomicity rules, but compound operations are not automatically atomic.

```java
count++;
```

is the classic example.

---

# 11. CAS, Atomics and Lock-Free Foundations

CAS = **Compare-And-Set / Compare-And-Swap** concept.

Conceptually:

```text
Current value == expected?
        |
   +----+----+
   |         |
  Yes        No
   |         |
write new   fail/retry
```

Example:

```java
AtomicInteger counter = new AtomicInteger();

counter.incrementAndGet();
```

Simplified retry model:

```text
read current
     |
     v
calculate new
     |
     v
CAS(current,new)
     |
  success? ---- yes ---> done
     |
     no
     |
    retry
```

CAS is fundamental to many concurrent data structures.

Important caveat:

> "Lock-free" does not mean "free." Under contention, repeated CAS failures can consume CPU.

### ABA Problem

A value can change:

```text
A → B → A
```

A CAS checking only the final value may not detect that intermediate change.

Versioning/stamped approaches can address ABA where it matters.

---

# 12. Monitors and `synchronized` Internals

Every Java object can conceptually participate in monitor synchronization.

```java
synchronized (lock) {
    criticalSection();
}
```

Bytecode for synchronized blocks involves instructions conceptually represented by:

```text
monitorenter
monitorexit
```

For synchronized methods, synchronization is represented through method metadata/access flags rather than explicit monitor instructions in the same form.

Know the conceptual state:

```text
Object / Monitor
       |
       +---- Owner thread
       |
       +---- Threads waiting to acquire
       |
       +---- wait()/notify() coordination
```

Do not rely on old interview diagrams that permanently describe specific historical biased/thin/heavy lock implementation states; locking implementation has evolved across modern JDK versions.

---

# 13. Garbage Collection Fundamentals

## What Is Garbage?

An object is eligible for reclamation when it is no longer reachable through the JVM's relevant root/reference graph.

Java primarily reasons about **reachability**, not simple reference counting.

```text
GC Roots
  |
  +------> A ------> B
  |
  +------> C

D ------> E

A,B,C = reachable
D,E   = unreachable if no root path exists
```

## GC Roots

Important categories include references associated with:

- live thread stacks
- static/class-related runtime roots
- JNI handles/references
- JVM internal structures

For interviews, understand the idea rather than memorizing every HotSpot root category.

## Strong Reachability

```java
Object obj = new Object();
```

As long as a live strong reference path exists, the object is not normally reclaimed.

## Reference Types

Java provides:

```text
Strong
Soft
Weak
Phantom
```

### WeakReference

```java
WeakReference<Object> ref =
        new WeakReference<>(new Object());
```

Weakly reachable objects can be reclaimed when GC determines them weakly reachable.

### SoftReference

Historically useful for memory-sensitive references, but generally not a good default mechanism for designing modern application caches.

### PhantomReference

Used with `ReferenceQueue` for advanced post-mortem/resource-management patterns.

For normal external resources, prefer deterministic cleanup:

```java
try (InputStream in = ...) {
}
```

---

# 14. Generational GC Concept

The generational hypothesis:

> Most objects die young.

Classic conceptual model:

```text
                 HEAP

+---------------------------------------------+
| Young Generation                            |
|                                             |
|  +-----------+ +---------+ +---------+      |
|  |   Eden    | |   S0    | |   S1    |      |
|  +-----------+ +---------+ +---------+      |
+------------------------+--------------------+
                         |
                         | surviving objects
                         v
+---------------------------------------------+
| Old Generation                              |
+---------------------------------------------+
```

Typical lifecycle:

```text
new object
    ↓
young allocation
    ↓
survives collections?
    |
    +-- No → reclaimed
    |
    Yes
    ↓
age increases / survives
    ↓
eventual promotion when collector policy chooses
    ↓
old generation
```

Exact mechanics depend on collector.

---

# 15. Garbage Collection Algorithms

Fundamental algorithms are more important than memorizing collector marketing names.

## Mark

Determine reachable/live objects.

```text
Root → A → B

Root → C

D → E

Mark:
A ✓
B ✓
C ✓
D ✗
E ✗
```

## Sweep

Reclaim unmarked memory.

Problem:

```text
| LIVE | FREE | LIVE | FREE | LIVE |
```

can create fragmentation.

## Compact

Move surviving objects together.

```text
Before:
| LIVE | FREE | LIVE | FREE | LIVE |

After:
| LIVE | LIVE | LIVE |      FREE      |
```

## Copying

Copy live objects from one area to another.

Useful when most objects in the source area are dead.

---

# 16. Major HotSpot Garbage Collectors

## Serial GC

```bash
-XX:+UseSerialGC
```

Characteristics:

- simple
- stop-the-world collection
- single GC worker for key collection work
- useful for small heaps/simple workloads

## Parallel GC

```bash
-XX:+UseParallelGC
```

Primary goal:

```text
Throughput
```

Uses multiple GC workers but still relies heavily on STW collection phases.

## CMS

CMS is historical knowledge.

```text
Deprecated: JDK 9
Removed:    JDK 14
```

Know why it mattered:

- concurrent old-generation collection
- lower pauses than older collectors
- fragmentation and complexity problems

Do not recommend it for modern Java.

## G1 GC

```bash
-XX:+UseG1GC
```

Default HotSpot collector for server-class configurations since Java 9.

Instead of fixed contiguous generation partitions, G1 divides the heap into many regions.

```text
G1 Heap

+-----+-----+-----+-----+
| E   | O   | E   | S   |
+-----+-----+-----+-----+
| O   | F   | O   | E   |
+-----+-----+-----+-----+
| H   | H   | O   | F   |
+-----+-----+-----+-----+

E = Eden
S = Survivor
O = Old
H = Humongous
F = Free
```

Region roles can change over time.

### Why "Garbage First"?

G1 attempts to prioritize regions that can provide useful garbage reclamation while working toward pause-time goals.

### G1 High-Level Cycle

```text
Young Collections
      |
      v
Concurrent Marking begins when needed
      |
      v
Identify old-region liveness
      |
      v
Mixed Collections
(young + selected old regions)
      |
      v
Continue normal operation
```

### Remembered Sets / Cross-Region References

A collector cannot scan the entire heap every time it collects a young region.

Conceptually:

```text
Old Region --------> Young Region
      cross-region reference
```

G1 maintains metadata that helps identify relevant cross-region references.

At interview level know:

- remembered sets track inter-region reference information
- write barriers help maintain GC metadata as references change

You do not need to memorize internal card-table bit layouts.

### Humongous Objects

Large objects can receive special treatment and occupy one or more contiguous G1 regions.

This can matter for:

- large arrays
- large buffers
- memory pressure

## ZGC

```bash
-XX:+UseZGC
```

Primary design goal:

```text
Very low GC pauses
```

Most expensive work is performed concurrently with application threads.

Conceptual comparison:

```text
Traditional STW-heavy collector

App =======|      GC      |=========| GC |======


Concurrent low-latency collector

App =============================================>
          \____ concurrent GC work ____/
              |pause|         |pause|
```

Modern ZGC has evolved significantly, including generational capabilities in newer JDKs. For interviews, know the architectural objective rather than memorizing version-specific pointer bit layouts.

## Shenandoah

Another low-pause collector that performs much of marking and compaction concurrently.

For most Java interviews:

```text
G1       → know well
ZGC      → understand purpose and high-level architecture
Parallel → understand throughput use case
Serial   → understand simple/small-heap use case
CMS      → historical
Shenandoah → overview
```

---

# 17. Safepoints and Stop-The-World

A **safepoint** is a state where the JVM can safely perform certain global VM operations with threads in a known manageable state.

Operations can include parts of:

- GC
- deoptimization
- some class/runtime operations
- diagnostic VM operations

Conceptually:

```text
Thread 1 ────────────────● paused
Thread 2 ─────────────●    paused
Thread 3 ─────────────────● paused
                        ^
                   safepoint reached

             JVM performs STW work
                        |
                        v

Threads resume ─────────────────────>
```

Important distinction:

```text
Safepoint ≠ Garbage Collection
```

GC is one major reason safepoints/STW pauses can occur.

---

# 18. String Internals and String Pool

## String Literal Pool

```java
String a = "hello";
String b = "hello";

System.out.println(a == b);
```

Typically:

```text
true
```

because both literal references use the same interned string object.

Conceptual:

```text
String Pool / Heap

+-----------+
| "hello"   |
+-----------+
   ↑     ↑
   a     b
```

## `new String`

```java
String a = "hello";
String b = new String("hello");

System.out.println(a == b);
```

Normally:

```text
false
```

because `b` refers to a distinct `String` object.

## `intern()`

```java
String canonical = b.intern();
```

returns the canonical pooled representation for that string value according to `String.intern()` semantics.

## Modern String Representation

Since Java 9, HotSpot's standard `String` implementation uses **Compact Strings** where possible, internally using a `byte[]` plus encoding/coder information rather than always using a `char[]`.

Interview value:

- saves memory for strings representable in compact encodings
- demonstrates that Java object implementation can evolve without changing normal `String` API semantics

---

# 19. Reflection and JVM Relationship

Reflection lets Java inspect and interact with runtime type metadata.

```java
Class<?> clazz = Class.forName("com.example.Employee");

Method method = clazz.getDeclaredMethod("work");

Object employee = clazz.getDeclaredConstructor().newInstance();

method.invoke(employee);
```

Used heavily by frameworks for:

- dependency injection
- serialization
- ORM
- testing
- runtime discovery

Modern JVM/framework designs also use:

- method handles
- generated bytecode
- proxies

because reflection is not the only dynamic invocation mechanism.

---

# 20. JNI and Native Code

JNI = Java Native Interface.

Example declaration:

```java
public native int calculate(int value);
```

JNI allows Java to interact with native libraries.

Conceptually:

```text
Java
  |
  | JNI
  v
Native C/C++ Library
  |
  v
Operating System / Native API
```

Risks:

- native memory leaks
- process crashes
- unsafe memory access
- portability complexity

Unlike ordinary Java exceptions, some native failures can terminate the entire JVM process.

---

# 21. Off-Heap / Native Memory

The JVM process uses far more memory than only `-Xmx`.

```text
JVM Process Memory
│
├── Java Heap
├── Metaspace
├── Code Cache
├── Thread Stacks
├── Direct Buffers
├── GC Native Structures
├── JVM Internal Native Memory
└── Native Libraries
```

Therefore:

> `-Xmx4g` does NOT mean the Java process will use at most 4 GB.

## Direct ByteBuffer

```java
ByteBuffer buffer =
        ByteBuffer.allocateDirect(1024);
```

Direct buffers use memory outside the ordinary Java heap for their backing storage.

Useful for I/O because native operations can often work efficiently with direct memory.

## Native Memory Tracking

Useful diagnostic capability:

```bash
-XX:NativeMemoryTracking=summary
```

Then:

```bash
jcmd <pid> VM.native_memory summary
```

This is extremely useful when:

```text
Heap looks healthy
       +
Process RSS is huge
       ↓
Investigate native/off-heap memory
```

---

# 22. JVM Errors You Must Understand

## `StackOverflowError`

Typical cause:

```text
Deep/infinite call stack
```

Investigate:

- recursion
- stack traces
- `-Xss` only after understanding why stack depth is excessive

## `OutOfMemoryError: Java heap space`

Possible causes:

- genuine heap too small
- unbounded cache
- retained collections
- application memory leak
- workload exceeds design assumptions

Do **not** immediately solve every heap OOM by increasing `-Xmx`.

## `OutOfMemoryError: GC overhead limit exceeded`

The JVM spends excessive time collecting while recovering very little memory.

Usually indicates severe heap pressure.

## `OutOfMemoryError: Metaspace`

Investigate:

- excessive class loading
- dynamically generated classes
- classloader leak
- Metaspace configuration

## `OutOfMemoryError: Direct buffer memory`

Investigate:

- direct buffers
- NIO/Netty configuration
- buffer lifecycle
- native memory limits

## `OutOfMemoryError: unable to create native thread`

Possible causes:

- excessive thread count
- OS process/thread limits
- insufficient native memory for new thread stacks
- container limits

Important:

```text
Heap free
   ≠
JVM can always create another thread
```

because thread stacks consume native process memory.

---

# 23. JVM Diagnostics and Production Troubleshooting

Knowing theory without diagnostics is incomplete JVM knowledge.

## `jcmd`

The most useful general-purpose HotSpot diagnostic command.

```bash
jcmd <pid> help
```

Examples:

```bash
jcmd <pid> VM.flags
jcmd <pid> VM.system_properties
jcmd <pid> Thread.print
jcmd <pid> GC.heap_info
jcmd <pid> GC.class_histogram
```

## Thread Dump

```bash
jcmd <pid> Thread.print
```

or:

```bash
jstack <pid>
```

Use for:

- deadlocks
- blocked threads
- high thread count
- stuck requests
- CPU investigation

## Heap Dump

A common option:

```bash
jcmd <pid> GC.heap_dump /tmp/heap.hprof
```

Automatic heap dump on OOM:

```bash
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp
```

Analyze with tools such as Eclipse MAT or suitable profilers.

## Class Histogram

```bash
jcmd <pid> GC.class_histogram
```

Useful for quickly seeing:

```text
Which object types dominate?
How many instances exist?
How much shallow memory do they consume?
```

## `jstat`

Useful for observing JVM/GC statistics.

Example:

```bash
jstat -gcutil <pid> 1000
```

## GC Logging

Modern unified logging:

```bash
-Xlog:gc*
```

More detailed example:

```bash
-Xlog:gc*:file=gc.log:time,uptime,level,tags
```

## Java Flight Recorder — JFR

JFR is one of the most important production JVM tools.

It can capture information about:

- CPU activity
- allocations
- GC
- locks
- threads
- I/O
- exceptions
- JVM events

Example:

```bash
jcmd <pid> JFR.start name=profile duration=60s filename=recording.jfr
```

## Java Mission Control — JMC

Used to inspect JFR recordings.

## VisualVM

Useful for learning and development-time observation of:

- heap
- CPU
- threads
- GC behavior

---

# 24. Practical JVM Troubleshooting Playbook

## Scenario 1 — High CPU

```text
High CPU
   |
   v
Identify JVM PID
   |
   v
Find hot OS thread
   |
   v
Take thread dumps / JFR
   |
   v
Map hot thread to Java work
   |
   v
Look for:
 loops / serialization / regex /
 lock contention / excessive GC /
 application hot methods
```

JFR is often the best starting point when available.

## Scenario 2 — Application Hangs

```text
Application not responding
        |
        v
Take multiple thread dumps
        |
        v
Compare thread states
        |
        +--> BLOCKED?
        |
        +--> WAITING?
        |
        +--> deadlock?
        |
        +--> external I/O?
        |
        +--> thread-pool exhaustion?
```

## Scenario 3 — Memory Continuously Increasing

```text
Memory increasing
      |
      v
Is Java heap increasing?
  |                 |
 Yes               No
  |                 |
  v                 v
Heap investigation  Native memory investigation
  |                 |
Histogram            NMT
Heap dump            Direct buffers
JFR allocations      Thread count/stacks
Retained graph       Metaspace
```

## Scenario 4 — Frequent GC

Check:

- allocation rate
- live-set size
- heap sizing
- promotion behavior
- large temporary allocations
- cache growth
- collector logs

Do not tune random GC flags before identifying the cause.

## Scenario 5 — Long GC Pauses

Investigate:

```text
GC logs
   +
JFR
   +
Heap occupancy
   +
Allocation rate
   +
Collector type
   +
Container CPU/memory limits
```

---

# 25. JVM Configuration Essentials

## Heap

```bash
-Xms
-Xmx
```

Example:

```bash
-Xms2g -Xmx2g
```

## Stack

```bash
-Xss
```

## Metaspace

```bash
-XX:MaxMetaspaceSize=
```

Use a hard maximum only when it serves an operational purpose; an arbitrarily low value can itself cause failures.

## GC

```bash
-XX:+UseG1GC
-XX:+UseParallelGC
-XX:+UseZGC
```

## G1 Pause Target

```bash
-XX:MaxGCPauseMillis=200
```

Remember:

```text
target ≠ guarantee
```

## Heap Dump on OOM

```bash
-XX:+HeapDumpOnOutOfMemoryError
```

## GC Logging

```bash
-Xlog:gc*
```

---

# 26. JVM in Containers

Modern Java is container-aware, but this topic is essential for backend engineers.

A JVM running in Kubernetes/Docker is constrained by container resources.

```text
Physical Server
   |
   +----------------------------------+
   | Container                        |
   |                                  |
   | CPU Limit                        |
   | Memory Limit                     |
   |                                  |
   | JVM                              |
   |  ├── Heap                        |
   |  ├── Metaspace                   |
   |  ├── Stacks                      |
   |  ├── Direct Memory               |
   |  └── JVM Native Memory           |
   +----------------------------------+
```

Critical rule:

```text
Container Memory Limit
        >
-Xmx
```

because heap is only one part of process memory.

If:

```text
container limit = 4 GB
-Xmx = 4 GB
```

the process can still be killed because Metaspace, thread stacks, direct memory and JVM native structures require additional memory.

---

# 27. Common JVM Interview Traps

## Trap 1

**Question:** Are all objects created in Heap?

Weak answer:

> Yes, always.

Better answer:

> Java's object memory is conceptually heap-managed, but HotSpot JIT escape analysis/scalar replacement can eliminate some allocations, so a physical heap allocation is not guaranteed.

## Trap 2

**Question:** Does `volatile` make `count++` thread-safe?

```java
volatile int count;
count++;
```

Answer:

> No. `volatile` provides visibility/order guarantees but does not make the compound read-modify-write operation atomic.

## Trap 3

**Question:** Is Metaspace part of Heap?

Answer:

> No. In HotSpot, class metadata stored in Metaspace uses native memory outside the Java heap.

## Trap 4

**Question:** Does GC eliminate memory leaks?

Answer:

> No. GC reclaims unreachable objects. Objects that are no longer useful but remain strongly reachable can still leak memory.

## Trap 5

**Question:** Does `-Xmx4g` limit the JVM process to 4 GB?

Answer:

> No. It limits the maximum Java heap. The JVM also consumes Metaspace, Code Cache, stacks, direct memory and other native memory.

## Trap 6

**Question:** Is G1 completely concurrent?

Answer:

> No. G1 performs concurrent work but still has stop-the-world phases.

## Trap 7

**Question:** Is `MaxGCPauseMillis=200` guaranteed?

Answer:

> No. It is a pause-time goal.

## Trap 8

**Question:** Is class identity only the fully-qualified name?

Answer:

> No. Class identity also depends on its defining ClassLoader.

## Trap 9

**Question:** Does `new` always mean expensive global heap synchronization?

Answer:

> No. HotSpot commonly uses TLABs and bump-pointer allocation, making normal allocation extremely cheap; some allocations can also be optimized away.

## Trap 10

**Question:** Are Major GC and Full GC always identical?

Answer:

> No. Terminology varies. Prefer collector-specific terms such as Young GC, Mixed GC and Full GC.

---

# 28. End-to-End Example — What Happens When Java Executes a Method?

Consider:

```java
public class OrderService {

    private static final int TAX = 10;

    public static void main(String[] args) {
        OrderService service = new OrderService();
        int total = service.calculate(100);
        System.out.println(total);
    }

    public int calculate(int amount) {
        return amount + TAX;
    }
}
```

## Complete Flow

```text
OrderService.java
       |
       | javac
       v
OrderService.class
       |
       | java OrderService
       v
Native Java launcher starts JVM
       |
       v
Application ClassLoader requests OrderService
       |
       v
Parent delegation
       |
       v
Load OrderService bytecode
       |
       v
Verification
       |
       v
Preparation
       |
       v
Resolution as required
       |
       v
Initialization
       |
       v
main() stack frame created
       |
       v
new OrderService()
       |
       +--> allocation may use TLAB
       |
       v
reference stored in main frame
       |
       v
calculate(100)
       |
       v
new calculate() frame
       |
       +--> local variable: this
       +--> local variable: amount
       +--> operand stack performs addition
       |
       v
result returned to main frame
       |
       v
System.out.println(total)
       |
       v
Interpreter initially executes bytecode
       |
       v
If code becomes hot:
 profiling → JIT → optimized native code
       |
       v
Objects eventually unreachable?
       |
       v
GC may reclaim them
```

If you can explain every arrow in this diagram, your JVM foundation is strong.

---

# 29. Interview Questions You Should Be Able to Answer

### Architecture

1. What exactly is JVM?
2. JVM vs JRE vs JDK?
3. Why is Java platform independent?
4. What happens internally after `java Main`?
5. What are the JVM runtime data areas?

### Class Loading

6. Explain Loading, Linking and Initialization.
7. What happens during Verification?
8. Preparation vs Initialization?
9. What is symbolic resolution?
10. Explain parent delegation.
11. Why can't application classes normally replace `java.lang.String`?
12. What is a custom ClassLoader?
13. Can the same class be loaded twice?
14. What defines class identity?
15. When is a class initialized?

### Memory

16. Heap vs Stack?
17. What is stored in Metaspace?
18. Method Area vs Metaspace?
19. What is the PC register?
20. What is a stack frame?
21. What is the operand stack?
22. What causes StackOverflowError?
23. What is direct memory?
24. Why can process memory exceed `-Xmx`?
25. What is TLAB?

### Object Internals

26. Explain HotSpot object layout.
27. What is Mark Word?
28. What is Klass Pointer?
29. What is object padding?
30. What are compressed OOPs?

### Execution / JIT

31. Interpreter vs JIT?
32. What makes code "hot"?
33. C1 vs C2?
34. What is tiered compilation?
35. What is method inlining?
36. What is escape analysis?
37. What is scalar replacement?
38. What is deoptimization?
39. What is Code Cache?

### JMM / Concurrency

40. What is the Java Memory Model?
41. Visibility vs atomicity vs ordering?
42. What exactly does `volatile` guarantee?
43. Why is `volatile int count; count++` unsafe?
44. What is happens-before?
45. What does `synchronized` guarantee?
46. What is CAS?
47. What is the ABA problem?
48. How does monitor synchronization relate to JVM bytecode?

### GC

49. How does JVM know an object is garbage?
50. What are GC Roots?
51. Why doesn't Java use simple reference counting?
52. Explain generational hypothesis.
53. Mark vs Sweep vs Compact vs Copy?
54. Young GC vs Full GC?
55. What is STW?
56. What is a safepoint?
57. Explain G1 regions.
58. What is a G1 mixed collection?
59. What are remembered sets conceptually?
60. What is a write barrier?
61. What is a humongous object?
62. G1 vs ZGC?
63. Parallel GC vs G1?
64. Why can GC pauses still happen with concurrent collectors?

### Troubleshooting

65. How would you debug high JVM CPU?
66. How would you debug a memory leak?
67. Heap dump vs thread dump?
68. What does `jcmd` do?
69. What is JFR?
70. What is Native Memory Tracking?
71. How do you debug `unable to create native thread`?
72. Why can Kubernetes kill a JVM even when heap is below `-Xmx`?
73. How would you investigate frequent Full GCs?
74. How would you investigate a deadlock?

---

# 30. Final JVM Mental Model

Do not memorize JVM as disconnected topics.

Remember this chain:

```text
SOURCE CODE
    |
    v
COMPILATION
.java → .class / bytecode
    |
    v
CLASS LOADING
Loading → Linking → Initialization
    |
    v
RUNTIME MEMORY
Heap + Method Area/Metaspace + Per-thread stacks
    |
    v
EXECUTION
Interpreter → Profiling → JIT → Native Code
    |
    v
CONCURRENCY
JMM → happens-before → volatile / monitors / CAS
    |
    v
MEMORY MANAGEMENT
Allocation → TLAB → Reachability → GC
    |
    v
PRODUCTION
JFR + jcmd + dumps + GC logs + NMT
```

## What You Must Know Deeply

```text
★★★★★ Class Loading
★★★★★ Heap / Stack / Metaspace
★★★★★ Stack Frames
★★★★★ JIT and Escape Analysis
★★★★★ Java Memory Model
★★★★★ volatile / synchronized / CAS
★★★★★ GC Roots and Reachability
★★★★★ G1
★★★★★ JVM Diagnostics

★★★★☆ Bytecode
★★★★☆ Object Layout
★★★★☆ Off-Heap Memory
★★★★☆ Safepoints
★★★★☆ JVM in Containers

★★★☆☆ ZGC internals
★★★☆☆ JNI
★★★☆☆ Reflection internals
★★☆☆☆ Historical CMS details
```

---

# JVM Mastery Completion Checklist

- [ ] I can explain `.java → bytecode → JVM → machine code`.
- [ ] I can explain Loading, Linking and Initialization without notes.
- [ ] I understand parent delegation and class identity.
- [ ] I understand Heap, Stack, Method Area and Metaspace.
- [ ] I can draw a stack frame and explain the operand stack.
- [ ] I understand the basic HotSpot object layout.
- [ ] I understand TLAB and why allocation can be cheap.
- [ ] I can explain Interpreter, C1, C2 and tiered compilation.
- [ ] I understand inlining, escape analysis and scalar replacement.
- [ ] I can explain JMM using visibility, ordering and atomicity.
- [ ] I can explain `volatile`, `synchronized` and happens-before.
- [ ] I understand CAS and its limitations.
- [ ] I can explain GC Roots and reachability.
- [ ] I understand Mark, Sweep, Compact and Copy.
- [ ] I understand the generational hypothesis.
- [ ] I can explain G1 regions, young collections and mixed collections.
- [ ] I understand what ZGC is trying to solve.
- [ ] I understand safepoints and STW.
- [ ] I understand heap vs native/off-heap memory.
- [ ] I know the major `OutOfMemoryError` categories.
- [ ] I can use `jcmd`, thread dumps, heap dumps, GC logs and JFR conceptually.
- [ ] I know why JVM memory planning is important inside containers.
- [ ] I can walk through the complete lifecycle of a Java method from class loading to execution and GC.

> If every item above can be explained in your own words with a small example, you have covered the essential JVM internals expected across Java developer, senior backend, lead and architecture-oriented interviews.

