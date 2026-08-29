# JVM Runtime Data Areas

The JVM uses several runtime data areas to store class information, objects, method execution state, and native execution state.

```text
JVM Process
│
├── Shared Areas
│   ├── Heap
│   ├── Method Area / Metaspace
│   └── Other Native Memory
│
├── Thread-1
│   ├── Java Stack
│   ├── PC Register
│   └── Native Method Stack
│
├── Thread-2
│   ├── Java Stack
│   ├── PC Register
│   └── Native Method Stack
│
└── Execution Engine
    ├── Interpreter
    ├── JIT Compiler
    └── Garbage Collector
```

---

# 1. Method Area and Metaspace

The **Method Area** is a logical JVM runtime area defined by the JVM specification.

It stores information related to loaded classes.

The specification describes the Method Area as logically part of the Heap, but it defines the Method Area and Heap as distinct runtime areas with different purposes. This does not mean HotSpot stores class metadata in the ordinary garbage-collected Java object heap.

In modern HotSpot JVMs, class metadata is stored mainly in **Metaspace**, which uses native memory.

## What Metaspace stores

For a loaded class, Metaspace contains runtime class metadata such as:

- Class metadata
- Field metadata
- Method metadata
- Runtime Constant Pool
- Information required by the JVM to execute methods
- Class hierarchy and access information

Example:

```java
class Employee {

    static int count = 0;

    void work() {
    }
}
```

Conceptually:

```text
Metaspace

Employee
│
├── Class Metadata
├── Field Metadata
│   └── count
├── Method Metadata
│   ├── <init>()
│   └── work()
└── Runtime Constant Pool
```

Ordinary Java objects are not stored in Metaspace.

```java
Employee employee = new Employee();
```

The `Employee` object is normally stored in the Heap.

## Method Area vs Metaspace

```text
Method Area
    → JVM specification concept

Metaspace
    → HotSpot implementation area used mainly for class metadata
```

The JVM specification says that the Method Area stores per-class structures such as the runtime constant pool, field and method data, and the code for methods and constructors. It deliberately does not mandate a physical location or memory-management policy for this logical area.

### Where static fields and method bytecode live

It is useful to separate a language-level concept from a physical HotSpot layout:

- The Method Area contains the runtime structures that describe each field and method, including method bytecode and constructor bytecode before or alongside compiled forms.
- A `static` field belongs to its declaring class rather than to an object instance. The JVM specification defines its class-level behavior but does not require its value to occupy a particular physical Method Area address.
- In HotSpot, class metadata is primarily in Metaspace, while the associated `java.lang.Class` mirror is a heap object. Do not rely on the interview shortcut "all static variables are stored in Metaspace."
- JIT-compiled native instructions are stored separately in HotSpot's native Code Cache, not in the Java Heap.

### PermGen vs Metaspace

```text
HotSpot through JDK 7     Permanent Generation (PermGen)
HotSpot from JDK 8       Native Metaspace replaces PermGen
```

PermGen was a HotSpot implementation of class-metadata storage, not a runtime area required by the JVM specification. JDK 8 removed PermGen and introduced native-memory Metaspace. Metaspace can grow according to demand and available native memory unless constrained, for example with `-XX:MaxMetaspaceSize`; it is not unlimited in practice.

## Class Loading Flow

When a class is required:

```text
.class File
    │
    v
ClassLoader
    │
    v
Loading
    │
    v
Linking
├── Verification
├── Preparation
└── Resolution
    │
    v
Initialization (when required)
    │
    v
Class ready for use
```

Loading and linking do not automatically imply immediate initialization. A class can remain loaded/linked without being initialized until a JVM-defined initialization trigger occurs.

The ClassLoader is a loading mechanism, not a memory area.

```text
ClassLoader
    │
    v
JVM creates runtime class structures
    │
    ├── Class Metadata → Metaspace
    │
    └── java.lang.Class object → Heap
```

## Runtime Constant Pool

Each loaded class has a Runtime Constant Pool derived from the constant pool inside its `.class` file.

It contains information such as:

- Class references
- Field references
- Method references
- Interface references
- Numeric constants
- String literals and symbolic references

During execution, symbolic references can be resolved into runtime references.

The Runtime Constant Pool is not the same thing as the **String intern pool**. Interned strings are ordinary `String` objects; in modern HotSpot, those objects live on the Java Heap. A runtime constant-pool entry for a string literal can resolve to a reference to the corresponding interned `String` object.

---

# 2. Heap Memory

The **Heap** is the shared JVM runtime area where Java objects and arrays are generally allocated.

Example:

```java
Employee employee = new Employee();
```

Conceptually:

```text
Java Stack                     Heap

employee reference ----------> Employee Object
```

The Heap is shared by all Java threads.

## What Heap stores

- Java objects
- Arrays
- `java.lang.Class` objects
- Strings
- Collections
- Application data
- Objects referenced by static fields
- Other normal Java runtime objects

References to heap objects can exist in:

- Stack frame local variables
- Other heap objects
- Static fields
- JNI references
- JVM internal structures

## HotSpot object layout

Object layout is a HotSpot implementation topic, not a Java-language or JVM-specification guarantee. A traditional HotSpot object is often described as:

```text
Object
├── header
│   ├── mark/status information
│   └── class-metadata reference
├── instance-field data
└── alignment padding, when needed
```

- Header state can participate in identity hashing, GC bookkeeping, and synchronization. Its exact bit layout varies with JDK version, collector, architecture, and object state.
- The class-metadata reference associates the object with its HotSpot class representation.
- HotSpot may arrange fields and add padding to satisfy layout and alignment requirements; do not infer an exact object size from source declarations alone.
- Arrays also carry their length as implementation metadata.

Suitable HotSpot configurations can use **compressed ordinary object pointers (compressed oops)** and **compressed class pointers** to reduce reference/header footprint. Inspect the running JVM rather than assuming they are enabled:

```text
java -XX:+PrintFlagsFinal -version

UseCompressedOops
UseCompressedClassPointers
```

JDK 25 also provides compact object headers as a product feature, but they are not the default layout. Compact headers can combine header information differently, so older diagrams showing a permanently separate mark word and class-pointer word are not universal.

## String literals, interning, and Compact Strings

String literals are represented by canonical interned `String` objects:

```java
String a = "hello";
String b = "hello";
System.out.println(a == b); // true for these literal references
```

An explicit construction normally produces a distinct object:

```java
String c = new String("hello");
System.out.println(a == c);          // false
System.out.println(a == c.intern()); // true
```

`String.intern()` returns the canonical representation for an equal string according to its API contract. Interned strings are still ordinary heap objects; the String pool is not PermGen or Metaspace in modern HotSpot.

Since Java 9, HotSpot normally uses **Compact Strings**: `String` stores content in a `byte[]` together with encoding/coder state, using a compact Latin-1 representation when possible and UTF-16 representation otherwise. This is an internal memory optimization and does not change `String`'s public UTF-16-based API semantics.

## Heap Characteristics

### Shared Memory

All Java threads can access heap objects if they hold references to them.

```text
Thread-A ──┐
           │
           v
       Shared Object
           ^
           │
Thread-B ──┘
```

Shared mutable objects may require synchronization.

### Dynamic Allocation

Objects are created dynamically during application execution.

The JVM specification defines the Heap as the runtime area from which storage for class instances and arrays is allocated. HotSpot may optimize an allocation away through techniques such as escape analysis and scalar replacement, but that optimization must preserve behavior as if the object followed normal heap semantics.

### Garbage Collected

When objects become unreachable, their heap storage becomes eligible for automatic reclamation by the Garbage Collector. Eligibility does not guarantee immediate collection or provide a deterministic collection time.

### Common heap misconceptions

- **"Heap objects have global scope"** — An object is accessible only through references allowed by Java visibility and synchronization rules. The Heap is shared, but an object is not automatically visible or reachable everywhere.
- **"Heap access is always slower than stack access"** — This is not a JVM guarantee. Performance depends on generated code, cache locality, object layout, indirection, escape analysis, and many other optimizations.
- **"Java manually deallocates heap memory"** — Java uses automatic storage management. Setting a reference to `null` does not free an object immediately; it only removes one reference and may make the object unreachable.
- **"GC prevents memory leaks and OutOfMemoryError"** — GC cannot reclaim reachable objects retained accidentally, and several heap and non-heap resource failures can still produce `OutOfMemoryError`.

## **GC Roots — Starting Point of Reachability Analysis**

**GC does not simply scan the Heap and guess which objects are being used.**

**Garbage Collection starts from a set of special references called GC Roots and follows references from those roots into the Heap.**

```text
                    GC ROOTS
                       │
        ┌──────────────┼───────────────┐
        │              │               │
        v              v               v
 Thread Stacks   Static/Class      JNI / JVM
 Local Refs      Related Roots       Roots
        │              │               │
        └──────────────┼───────────────┘
                       │
                       v
                    HEAP
                       │
               Follow references
                       │
          ┌────────────┴────────────┐
          │                         │
          v                         v
     Reachable Objects        Unreachable Objects
          │                         │
          v                         v
        KEEP                 Eligible for GC
```

**Think of GC Roots as entry points from outside normal Heap object-to-object references.**

The GC begins from these roots and recursively follows references:

```text
GC Root
   │
   v
Object-A
   │
   v
Object-B
   │
   v
Object-C
```

If an object can be reached through this reference graph, it is considered **live**.

```text
GC Root → A → B → C

A, B, C = Reachable
```

An object may still exist physically in Heap memory but have no path from any GC Root:

```text
GC Roots → A → B

             X → Y
```

Here:

```text
A, B → reachable

X, Y → unreachable
       → eligible for garbage collection
```

### Important GC Roots

Common GC Roots include:

- **References from active Java Stack Frames**
  - Local variables and parameters that reference heap objects
- **Static / class-related references**
  - Runtime roots associated with loaded classes
- **JNI references**
  - References held by native code through JNI
- **JVM internal references**
  - Internal JVM/runtime objects that must remain alive
- **Active thread-related roots**
  - Runtime structures associated with live threads

The key mental model is:

> **GC Roots start from runtime areas outside normal Heap object graphs and point into the Heap. The GC follows those references through the Heap to determine which objects are still reachable.**

```text
Java Stack ─────────────┐
                       │
Static/Class Roots ─────┤
                       │
JNI Roots ──────────────┼──→ Heap Objects → More Heap Objects
                       │
JVM/Internal Roots ─────┤
                       │
Thread Roots ───────────┘
```

Therefore:

**No path from any GC Root → object is unreachable → object becomes eligible for garbage collection.**

## Heap Size

Common JVM options:

```text
-Xms<size>    Initial heap size
-Xmx<size>    Maximum heap size
```

Example:

```bash
java -Xms512m -Xmx2g MyApplication
```

Without explicit sizes, HotSpot chooses heap settings ergonomically from resources visible to the JVM, including platform and supported container limits. Exact defaults vary by JDK and environment; they are not determined by one universal CPU/RAM/OS formula. `-Xss` configures the stack size of each platform thread and is not a heap option.

---

# 3. Heap Generations

Many garbage collectors organize or logically classify objects according to lifetime.

A common model is:

```text
Heap
│
├── Young Generation
│   ├── Eden
│   ├── Survivor 0
│   └── Survivor 1
│
└── Old Generation
```

The exact physical layout depends on the garbage collector.

For example, G1 uses many heap regions and dynamically assigns them roles such as Eden, Survivor, and Old.

## Object Lifecycle

A typical object lifecycle is:

```text
Object Created
     │
     v
Young Generation
     │
     v
Young GC
     │
     ├── Unreachable → reclaimed
     │
     └── Reachable
             │
             v
         Survivor
             │
       survives more GCs
             │
             v
       Old Generation
```

## Eden

Most ordinary short-lived objects are initially allocated in young-generation allocation space, commonly referred to as Eden.

Example:

```java
new Employee();
new Order();
new ArrayList<>();
```

## TLAB

HotSpot commonly uses **Thread-Local Allocation Buffers (TLABs)**.

A TLAB is a small portion of heap allocation space reserved for one thread.

```text
Young Heap / Eden-like Space
│
├── Thread-A TLAB
├── Thread-B TLAB
└── Shared Remaining Space
```

This allows many small object allocations to be performed efficiently.

Objects allocated through a TLAB are still normal heap objects.

Allocation from a TLAB can often use a bump-pointer fast path: advance the thread-local allocation pointer, initialize the object, and return its reference without a global heap lock. Large objects, TLAB refill, insufficient space, and collector-specific cases use slower paths.

## Survivor Spaces

In the traditional generational model:

```text
Eden
S0
S1
```

During a young GC:

```text
Before GC

Eden
S0
S1

       │
       v

After GC

Dead objects → reclaimed
Live objects → copied/retained in survivor space
```

S0 and S1 can alternate source and destination roles.

## Object Age

Objects that survive young collections accumulate an age.

```text
Age 0
  │
Young GC
  v
Age 1
  │
Young GC
  v
Age 2
  │
...
  v
Old Generation
```

Promotion decisions depend on the garbage collector, object age, survivor-space pressure, and JVM heuristics.

## Old Generation

Long-lived objects eventually reside in the old generation.

Typical examples:

- Long-lived caches
- Application configuration objects
- Framework objects
- Objects retained for a large part of the application's lifetime

---

# 4. Garbage Collection Terminology

## Young GC / Minor GC

A collection focused mainly on young-generation objects.

```text
Young Generation
      │
      v
Young GC
```

Young collections typically reclaim short-lived objects efficiently.

## Major GC

The term **Major GC** is used inconsistently.

It generally refers to garbage collection activity involving old-generation memory.

Do not assume Major GC always means Full GC.

## Full GC

A Full GC is a broader heap collection and is usually more expensive than a normal young collection.

The exact behavior depends on the garbage collector.

## G1 Example

G1 can reclaim old-generation regions through mixed collections.

```text
Young Regions
      +
Selected Old Regions
      │
      v
Mixed GC
```

Therefore, old-generation memory is not reclaimed only through Full GC.

## GC implementation boundary

This chapter owns Heap layout, reachability, generations, and collector-neutral terminology. Collection triggers, stop-the-world versus concurrent phases, Serial/Parallel/CMS/G1/ZGC/Shenandoah behavior, tuning flags, and pause/throughput trade-offs belong to [Execution Engine and Garbage Collection](Ch2-Execution%20Engine.md).

The important boundary is that "Eden is full" is not a universal GC trigger, Major and Full GC are not standardized synonyms, and a pause target is not a deadline. Interpret the selected collector's unified GC log rather than projecting one generational diagram onto every collector.

---

# 5. Java Stack

Every Java thread has its own Java Stack.

```text
Thread-1 → Stack-1
Thread-2 → Stack-2
Thread-3 → Stack-3
```

Stacks are not shared between threads.

The Java Stack stores execution state for active Java method calls.

Stack use is dynamic rather than "static memory allocation": frames are pushed as methods are invoked and popped after normal or abrupt completion. The JVM specification permits a stack to have a fixed size or to expand dynamically. A thread can receive `StackOverflowError` when its required stack exceeds the available size; failure to create or expand a stack can instead result in `OutOfMemoryError`.

## Stack Frame

Every Java method invocation creates one Stack Frame.

A Stack Frame belongs to the invoking thread's JVM Stack; it is not a Heap object in the JVM runtime-data-area model. A local variable inside the frame may hold a reference to an object on the Heap. Implementations may optimize physical representations, but the logical distinction remains important.

Example:

```java
main()
    -> service()
        -> repository()
```

Stack:

```text
Top
+--------------------+
| repository() Frame |
+--------------------+
| service() Frame    |
+--------------------+
| main() Frame       |
+--------------------+
Bottom
```

The stack follows **LIFO — Last In, First Out**.

When `repository()` returns:

```text
+--------------------+
| service() Frame    |
+--------------------+
| main() Frame       |
+--------------------+
```

## Stack Frame Structure

A stack frame contains:

```text
Stack Frame
│
├── Local Variable Array
├── Operand Stack
└── Frame Data
    ├── Method context
    ├── Return support
    └── Exception-handling information
```

A frame does not contain its own copy of the method bytecode.

## Local Variable Array

Stores:

- Method parameters
- Primitive local variables
- Object references
- Local temporary values

Example:

```java
public static int add(int x, int y) {
    int result = x + y;
    return result;
}
```

Conceptually:

```text
Local Variable Array

slot 0 → x
slot 1 → y
slot 2 → result
```

For an instance method, local variable slot `0` normally contains the `this` reference.

## Operand Stack

The Operand Stack is used by JVM bytecode instructions for intermediate calculations.

Example:

```java
int result = x + y;
```

Simplified execution:

```text
load x
    ↓
[x]

load y
    ↓
[x, y]

iadd
    ↓
[x + y]

store result
```

Relationship:

```text
Java Stack
    │
    └── Stack Frame
          │
          ├── Local Variables
          └── Operand Stack
```

## Local Variables and Thread Safety

Each method invocation has its own stack frame.

```text
Thread-A Stack            Thread-B Stack

num = 10                  num = 20
```

The local variable storage itself is thread-private.

However, a local reference can still point to a shared heap object.

```text
Thread-A local reference ──┐
                           v
                       Shared List
                           ^
Thread-B local reference ──┘
```

The shared heap object may require synchronization.

## Stack Size

Stack size is finite.

Common JVM option:

```text
-Xss<size>
```

Example:

```bash
java -Xss1m MyApplication
```

Each platform thread typically requires its own native stack memory.

---

# 6. StackOverflowError

`java.lang.StackOverflowError` occurs when a thread requires more stack space than is available.

Typical causes:

- Infinite recursion
- Very deep recursion
- Very deep method call chains
- Large stack frames

Example:

```java
public static void recurse() {
    recurse();
}
```

Conceptually:

```text
recurse()
recurse()
recurse()
recurse()
...
```

Eventually:

```text
java.lang.StackOverflowError
```

Memory failure distinction:

```text
Excessive Stack Usage
        ↓
StackOverflowError

Heap Allocation Failure
        ↓
OutOfMemoryError
```

---

# 7. PC Register

Every JVM thread has its own **Program Counter (PC) Register**.

```text
Thread-A → PC Register-A
Thread-B → PC Register-B
Thread-C → PC Register-C
```

The PC Register tracks the current JVM instruction position for the thread while executing a Java method.

Example bytecode:

```text
0: aload_0
1: getfield
4: iconst_1
5: iadd
6: ireturn
```

Conceptually:

```text
Current Method      = Employee.work()
Instruction Offset  = 5
```

## PC Register and Stack

The current stack frame identifies the active method invocation.

The PC Register identifies the execution position inside that method.

```text
Stack Frame
    ↓
Which method invocation?

PC Register
    ↓
Which JVM instruction?
```

Together:

```text
Method Invocation + Instruction Position
```

represent the JVM execution point of a thread.

When a native method is executing, the JVM specification does not require the PC register to contain a meaningful JVM bytecode position.

---

# 8. Native Method Stack

The Native Method Stack supports execution of native code.

Native code may be written in languages such as:

- C
- C++
- Rust
- Platform-specific native code

Java commonly interacts with native code using **JNI — Java Native Interface**.

Example:

```java
public native void performNativeOperation();
```

Execution flow:

```text
Java Method
    │
    v
JNI
    │
    v
Native Function
    │
    v
Operating System / Hardware
```

Conceptually:

```text
Java Thread
│
├── Java Stack
│   └── Java method frames
│
└── Native Method Stack
    └── Native execution frames
```

The exact implementation depends on the JVM and operating system.

In practice, native execution is often integrated with the operating-system thread stack.

---

# 9. Native Libraries

A native library contains compiled machine code that can be called by the JVM or Java application.

Examples:

```text
Windows → .dll
Linux   → .so
macOS   → .dylib
```

Example:

```java
System.loadLibrary("example");
```

Flow:

```text
Java Code
    │
    v
JNI
    │
    v
Native Library
    │
    v
Operating System / Hardware API
```

Native libraries are loaded into the JVM process's native address space.

They are not stored inside the Java Heap.

---

# 10. Native Memory

The Java process uses native memory in addition to the Java Heap.

```text
JVM Process Memory
│
├── Java Heap
│
└── Native / Non-Heap Memory
    ├── Metaspace
    ├── Thread Stacks
    ├── Code Cache
    ├── Direct Buffers
    ├── GC Data Structures
    ├── JNI / Native Allocations
    └── Native Libraries
```

Therefore:

```text
-Xmx
```

controls only the maximum Java Heap size.

The total operating-system memory used by a Java process can be much larger than `-Xmx`.

## Direct buffers and off-heap memory

A direct `ByteBuffer` has a normal Java object on the Heap while its backing storage is typically native/off-heap memory. That native storage counts toward process memory rather than the Java Heap and may be limited independently.

Reclamation tied to reachability and cleaner processing is not a deterministic substitute for an explicit resource lifetime. For new code that needs controlled native-memory lifetimes or foreign-function access, the Foreign Function and Memory API provides `MemorySegment` and `Arena` abstractions; JNI remains relevant for existing native integrations and capabilities outside that API.

## JVM memory inside containers

Modern HotSpot is container-aware on supported platforms and normally bases ergonomics on CPU and memory resources visible to the JVM. Container planning must include the entire process, not only `-Xmx`:

```text
Container memory limit
├── Java Heap
├── Metaspace and compressed class space
├── thread stacks
├── code cache
├── direct/off-heap buffers
├── GC and JVM native structures
└── JNI and third-party native allocations
```

Setting `-Xmx` equal to the container memory limit leaves no headroom and can cause the operating system/container runtime to kill the process without a catchable Java `OutOfMemoryError`. Verify the effective settings and resource view:

```text
java -XshowSettings:system -XshowSettings:vm -version
java -XX:+PrintFlagsFinal -version
```

Options such as `-XX:MaxRAMPercentage` influence ergonomic heap sizing, while `-XX:ActiveProcessorCount` can override the processor count used for JVM and library ergonomics. Explicit flags, JDK version/vendor, and the deployment's cgroup/container configuration all affect the final behavior.

---

# 11. Code Cache

The JIT compiler converts frequently executed Java bytecode into native machine code.

That generated native code is stored in the **Code Cache**.

```text
Java Bytecode
     │
     v
JIT Compiler
     │
     v
Native Machine Code
     │
     v
Code Cache
     │
     v
CPU
```

The Code Cache is native/non-heap memory.

---

# 12. Execution Boundary

Runtime data areas hold the state consumed and produced by execution: frames on JVM stacks, objects on the Heap, class information in the Method Area/Metaspace, the current instruction position in each PC register, and compiled code in the Code Cache. Interpreter/JIT lifecycle, optimizations, deoptimization, safepoints, JNI transitions, and collector algorithms are explained once in [Execution Engine and Garbage Collection](Ch2-Execution%20Engine.md).

---

# 13. Complete Runtime Example

```java
public class Employee {

    static int count = 0;

    private String name;

    public Employee(String name) {
        this.name = name;
        count++;
    }

    public int calculateSalary(int base) {
        int bonus = 1000;
        return base + bonus;
    }

    public static void main(String[] args) {
        Employee employee = new Employee("Arun");
        int salary = employee.calculateSalary(50000);
    }
}
```

Simplified runtime structure:

```text
JVM Process
│
├── Metaspace
│   └── Employee Class Metadata
│       ├── Field Metadata
│       │   ├── count
│       │   └── name
│       ├── Method Metadata
│       │   ├── <init>()
│       │   ├── calculateSalary()
│       │   └── main()
│       └── Runtime Constant Pool
│
├── Heap
│   ├── Employee Class Object
│   ├── Employee Object
│   │   └── name ───────────────┐
│   │                           │
│   └── String "Arun" <─────────┘
│
└── Main Thread
    │
    ├── Java Stack
    │   └── main() Frame
    │       ├── employee reference ──→ Employee Object
    │       ├── salary
    │       └── Operand Stack
    │
    ├── PC Register
    │   └── Current JVM instruction position
    │
    └── Native execution support
```

While `calculateSalary()` executes:

```text
Java Stack

+--------------------------------+
| calculateSalary() Frame        |
|                                |
| Local Variables                |
|   this  → Employee Object      |
|   base  = 50000                |
|   bonus = 1000                 |
|                                |
| Operand Stack                  |
+--------------------------------+
| main() Frame                   |
|   employee → Employee Object   |
+--------------------------------+
```

After `calculateSalary()` returns:

```text
calculateSalary() Frame
        ↓
      popped
```

The `Employee` object remains in Heap as long as it is reachable.

---

# 14. Shared vs Thread-Private Areas

| Area | Scope | Main Purpose |
|---|---|---|
| Heap | Shared | Java objects and arrays |
| Method Area | Shared | Logical JVM area for loaded-class information |
| Metaspace | Shared / Native | HotSpot class metadata |
| Java Stack | Per Thread | Method invocation frames |
| PC Register | Per Thread | Current JVM instruction position |
| Native Method Stack | Per Thread | Native execution support |
| Code Cache | Shared / Native | JIT-compiled machine code |
| Native Libraries | Process-wide | Platform-specific machine code |

---

# 15. Final JVM Memory Diagram

```text
                           JVM PROCESS
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  SHARED AREAS                                                │
│                                                              │
│  ┌───────────────────────┐   ┌────────────────────────────┐  │
│  │ Heap                  │   │ Metaspace                  │  │
│  │                       │   │                            │  │
│  │ Objects               │   │ Class Metadata             │  │
│  │ Arrays                │   │ Method Metadata            │  │
│  │ Strings               │   │ Field Metadata             │  │
│  │ Class Objects         │   │ Runtime Structures         │  │
│  └───────────────────────┘   └────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Native / Non-Heap Memory                              │  │
│  │ Code Cache | Direct Buffers | GC Data | JNI | Libs    │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  THREAD-A                         THREAD-B                    │
│  ┌───────────────────┐            ┌───────────────────┐      │
│  │ Java Stack        │            │ Java Stack        │      │
│  │ ┌───────────────┐ │            │ ┌───────────────┐ │      │
│  │ │ Stack Frame   │ │            │ │ Stack Frame   │ │      │
│  │ │ Local Vars    │ │            │ │ Local Vars    │ │      │
│  │ │ Operand Stack │ │            │ │ Operand Stack │ │      │
│  │ └───────────────┘ │            │ └───────────────┘ │      │
│  ├───────────────────┤            ├───────────────────┤      │
│  │ PC Register       │            │ PC Register       │      │
│  ├───────────────────┤            ├───────────────────┤      │
│  │ Native Stack      │            │ Native Stack      │      │
│  └───────────────────┘            └───────────────────┘      │
│                                                              │
│                 EXECUTION ENGINE                             │
│            Interpreter | JIT | GC                            │
│                       │                                      │
└───────────────────────┼──────────────────────────────────────┘
                        v
                       CPU
```

---

# 16. JVM GPS Analogy

Use this analogy only as a memory aid for the runtime areas.

```text
Metaspace / Class Metadata = Shared Map
Heap                       = Shared Warehouse
Thread                     = Vehicle
Java Stack                 = Journey Book
Stack Frame                = One Page / Current Stop
PC Register                = GPS Current Position
Native Method Stack        = Native Journey Support
Native Library             = Native Machine-Code Toolkit
CPU                        = Engine
```

## Metaspace = Shared Map

Metaspace contains metadata describing the classes and methods available to the JVM.

```text
Employee
├── fields
├── constructor
├── work()
└── calculateSalary()
```

Threads executing the same loaded class share this class-level runtime information.

## Heap = Shared Warehouse

Heap contains Java objects and arrays.

```text
Heap

Employee Object
Order Object
ArrayList Object
String Object
```

Different threads can reference the same object.

## Thread = Vehicle

A thread represents one independent path of execution.

```text
Thread-A
Thread-B
Thread-C
```

Each thread can move through methods independently.

## Java Stack = Journey Book

Each thread owns its own Java Stack.

It records the thread's active chain of method invocations.

```text
main()
   ↓
service()
   ↓
repository()
```

Stack:

```text
repository() Frame
service() Frame
main() Frame
```

## Stack Frame = One Journey Page

Each method invocation creates one Stack Frame containing:

```text
Stack Frame
├── Local Variables
├── Parameters
├── Operand Stack
└── Method / Return Context
```

When the method finishes, that frame is popped.

## PC Register = GPS Current Position

The Java Stack tells us:

```text
Which method invocation is active?
```

The PC Register tells us:

```text
Where inside that method's JVM instruction stream is execution currently located?
```

Example:

```text
Current Stack Frame
    → Employee.calculateSalary()

PC Register
    → bytecode offset 15
```

Together:

```text
Current Method Invocation + Current Instruction Position
```

give the JVM's current execution location for that thread.

```text
Thread
  │
  ├── Java Stack
  │      └── Current Frame = Which method?
  │
  └── PC Register
         └── Which instruction?
```

This is why the GPS analogy is useful:

> **Stack tells the route/current method context; PC Register tells the exact current instruction position.**

## Native Method Stack = Native Journey Support

When execution enters native code through JNI, native execution uses native stack/runtime mechanisms.

```text
Java Method
    ↓
JNI
    ↓
Native Method
```

## Native Library = Native Machine-Code Toolkit

Native libraries contain platform-specific compiled code.

```text
Java
 ↓
JNI
 ↓
.dll / .so / .dylib
 ↓
OS / Hardware
```

## CPU = Engine

The CPU executes native machine instructions.

```text
Java Bytecode
      │
      v
Interpreter / JIT
      │
      v
Machine Instructions
      │
      v
CPU
```

### One-Line Memory Trick

> **Metaspace is the shared map, Heap is the shared warehouse, Thread is the vehicle, Stack is its journey book, Stack Frame is one journey page, PC Register is the GPS position, and CPU is the engine executing machine instructions.**

---

# 17. End-to-End Execution Flow


For:

```java
Employee employee = new Employee("Arun");
employee.calculateSalary(50000);
```

the flow is:

```text
1. Employee class is required
        │
        v
2. ClassLoader loads Employee.class
        │
        v
3. JVM creates class metadata
        │
        ├── Metadata → Metaspace
        └── Class Object → Heap
        │
        v
4. new Employee(...)
        │
        v
5. Employee Object allocated in Heap
        │
        v
6. employee reference stored in current Stack Frame
        │
        v
7. calculateSalary(...) invoked
        │
        v
8. New Stack Frame created
        │
        ├── Parameters
        ├── Local Variables
        └── Operand Stack
        │
        v
9. PC Register tracks current JVM instruction
        │
        v
10. Interpreter / JIT executes code
        │
        v
11. CPU executes machine instructions
        │
        v
12. Method returns
        │
        v
13. Stack Frame is popped
        │
        v
14. Heap objects remain while reachable
        │
        v
15. Unreachable objects become eligible for GC
```

---

# 18. OutOfMemoryError Scenarios

`OutOfMemoryError` is a family of allocation failures, not a synonym for "the Java Heap is full." The detail message and surrounding diagnostics identify which resource failed.

| Typical detail message or symptom | What it normally indicates | First checks |
|---|---|---|
| `Java heap space` | The JVM could not allocate an object or array and could not make enough heap space available | Live-set trend, allocation rate, heap dump/histogram, `-Xmx`, retention paths |
| `GC overhead limit exceeded` | Supported GC policy detected very high GC time with very little recovery | Heap sizing, allocation pressure, retained objects, GC logs |
| `Requested array size exceeds VM limit` | Requested array length exceeds a VM implementation limit, regardless of available heap | Size calculation, overflow, input validation |
| `Metaspace` | Class metadata could not be allocated, possibly because of a limit or excessive class-loader/class generation | Class count, class-loader retention, `MaxMetaspaceSize`, native-memory headroom |
| `Compressed class space` | The compressed class-pointer metadata space was exhausted | Class loading/unloading and compressed class-space sizing |
| `unable to create native thread` | The process/OS could not create another platform thread because of native memory or OS/container limits | Thread count, `-Xss`, process limits, container PID/memory limits |
| `Direct buffer memory` | Direct `ByteBuffer` allocation reached its applicable limit or native memory was unavailable | Direct-buffer retention, cleaner delay, `MaxDirectMemorySize`, process memory |
| Native allocation or `Out of swap space?` message | A JVM or JNI/native allocation failed outside the Java Heap | Native Memory Tracking, OS memory, swap, native libraries, `-Xmx` headroom |

`StackOverflowError` is different: it usually means one thread exhausted its available stack through recursion, a deep call chain, or large frames. A stack creation/expansion failure can instead appear as `OutOfMemoryError`.

Recommended diagnostic startup options for environments where disk capacity and data policy permit them:

```text
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=<path>
-XX:StartFlightRecording=filename=<path>,settings=default
-Xlog:gc*,safepoint:file=<path>:time,uptime,level,tags
```

Heap dumps and recordings may contain sensitive application data and consume significant disk space. Plan storage, access, and retention before enabling them.

---

# 19. Memory Monitoring and Troubleshooting

Use evidence from the same workload phase in which the problem occurs. A development snapshot rarely represents production allocation rate, live set, concurrency, container limits, or traffic bursts.

General JFR/JMC, `jcmd`, `jstat`, `jmap`, VisualVM, unified-log, and Native Memory Tracking tool descriptions live in [Execution Engine and Garbage Collection](Ch2-Execution%20Engine.md#profiling-and-diagnostic-tools). This section applies those tools specifically to memory failures.

## Practical commands

```text
jcmd <pid> VM.flags
jcmd <pid> VM.command_line
jcmd <pid> GC.heap_info
jcmd <pid> GC.class_histogram
jcmd <pid> GC.heap_dump <file>
jcmd <pid> VM.native_memory summary
```

The exact command list is JVM-specific; use `jcmd <pid> help`. Heap histograms and especially heap dumps can pause or materially affect a large application, so check each command's reported impact before production use.

## A reliable troubleshooting order

1. Record the JDK vendor/version, JVM command line, collector, uptime, workload phase, host/container limits, and the exact error or latency symptom.
2. Decide whether the pressure is Heap, Metaspace/class loading, thread stacks/count, direct buffers, code cache, or other native memory.
3. Correlate GC logs, JFR events, application metrics, and OS/container metrics on the same timeline.
4. Compare **after-GC occupancy** over time. A rising post-GC live set suggests retention; high allocation with a stable live set suggests allocation pressure rather than a leak.
5. Use histograms or dumps to find dominant classes and paths from GC roots. Do not infer a leak solely from high pre-GC heap usage.
6. Change code, sizing, or collector settings only after forming a measurable hypothesis, then repeat the same workload.

---

# 20. Relationship to the Java Memory Model

The **Java Memory Model (JMM)** is not another physical JVM runtime data area. It defines concurrency rules for visibility, ordering, atomicity, data races, and happens-before relationships between threads.

```text
Runtime data areas
    → where execution state and data are represented conceptually

Java Memory Model
    → when one thread's actions and writes are guaranteed visible to another
```

Thread-private stacks do not make referenced objects thread-private. Two local references in separate frames may point to the same mutable Heap object, and correct synchronization is then governed by the JMM. Continue with [Java Memory Model notes](Ch3-java-memory-model-jmm-notes.md).

---

# 21. Preserved Handwritten Study Roadmap

The original architecture-note reminders are retained here as a checked roadmap:

- **Stack Frame Internals:** Covered in Sections 5–7. Correction: a frame belongs logically to a per-thread JVM Stack, not the Heap; its references may point to Heap objects.
- **JIT lifecycle:** Code Cache ownership is covered in Sections 11–12; interpreter profiling, C1/C2, OSR, optimization, deoptimization, and safepoints are owned by [Execution Engine and Garbage Collection](Ch2-Execution%20Engine.md).
- **JMM:** Its relationship to runtime areas is summarized in Section 20, with the detailed note linked there.
- **GC and algorithms:** Reachability, roots, generations, and terminology are covered in Sections 2–4. Collector mechanisms, triggers, choices, and tuning are owned by [Execution Engine and Garbage Collection](Ch2-Execution%20Engine.md#garbage-collection).
- **JVM monitoring and troubleshooting:** Covered in Section 19.
- **OutOfMemoryError scenarios:** Covered in Section 18, including heap and non-heap failures.

---

# 22. Authoritative References

- [JVM Specification, Chapter 2: runtime data areas and frames](https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-2.html)
- [JVM Specification, Chapter 5: loading, linking, and initialization](https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-5.html)
- [HotSpot VM GC Tuning Guide](https://docs.oracle.com/en/java/javase/25/gctuning/index.html)
- [HotSpot ergonomics: collector, heap, and tiered compiler defaults](https://docs.oracle.com/en/java/javase/25/gctuning/ergonomics.html)
- [JDK troubleshooting guide: diagnostic tools and NMT](https://docs.oracle.com/en/java/javase/25/troubleshoot/diagnostic-tools.html)
- [JDK troubleshooting guide: memory leaks and OutOfMemoryError](https://docs.oracle.com/en/java/javase/25/troubleshoot/troubleshooting-memory-leaks.html)
- [`jcmd` command reference](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jcmd.html)
- [HotSpot VM Guide: compressed oops, Compact Strings, and other performance enhancements](https://docs.oracle.com/en/java/javase/25/vm/java-virtual-machine-guide.pdf)
- [JDK 25 `java` command: container-aware and memory ergonomics options](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html)
- [OpenJDK JEP 519: Compact Object Headers](https://openjdk.org/jeps/519)
- [OpenJDK JEP 122: Remove the Permanent Generation](https://openjdk.org/jeps/122)
