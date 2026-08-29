# Points to Remember in JVM Internal Working

This file is intentionally a short revision checklist. Canonical explanations live in the linked topic chapters; this summary should not become a second full explanation of those subjects.

## Compilation and class loading

- `javac` does **not automatically compile every `.java` file in a project**. It compiles the source files supplied to it and may implicitly compile required source dependencies. IDEs and build tools commonly use incremental compilation.
- One source file can produce multiple `.class` files—for example, when it declares multiple top-level or nested types, anonymous/local classes, or other constructs that require additional class definitions.
- The Java launcher is given an **initial class**; the JVM does not scan the application looking for a class that contains `main`.
- Classes are commonly loaded on demand, but loading and some linking may occur eagerly. Initialization is more constrained and occurs only for reasons defined by the JVM specification.
- A class is loaded as one class definition. The JVM does not load only the static methods, static blocks, constructors, or individual members that happen to be used.
  - Runtime class structures include metadata for static and instance fields, methods, constructors, and the runtime constant pool.
  - An object's **instance-field values** belong to that object's Heap representation and are initialized when the object is created.
  - Method and constructor bytecode is associated with the loaded class's method metadata; JIT-compiled native code is stored separately in HotSpot's Code Cache.
- Built-in class loaders normally follow parent-first delegation: application/system → platform → bootstrap. Custom loaders normally preserve this behavior by overriding `findClass`, although specialized loading policies are possible.

## Loading, linking, and initialization

The class lifecycle has three logical phases; it is misleading to call the ClassLoader simply "the first component executed."

1. **Loading**
   - Finds or generates the binary representation of a class/interface.
   - Creates its JVM runtime representation and associated `java.lang.Class` object.
2. **Linking**
   - **Verification:** Checks class-file structure, bytecode constraints, and type safety.
   - **Preparation:** Creates static fields and assigns JVM default values such as `0`, `false`, or `null`. It does not execute Java bytecode.
   - **Resolution:** Converts symbolic references into runtime references. Resolution can occur eagerly or lazily within JVM-specification constraints.
3. **Initialization**
   - Assigns values from class-file `ConstantValue` attributes.
   - Initializes the required superclass and qualifying superinterfaces, if necessary.
   - Executes the class/interface initialization method, `<clinit>`, generated from other static field initializers and `static` blocks in source order.
   - The initial class is initialized before its entry-point method is invoked, so its static initialization runs before its conventional `main` method.

## Runtime data areas

| Area | Scope | What to remember |
|---|---|---|
| **Method Area** | Shared | Logical JVM area containing per-class structures such as the runtime constant pool, field/method data, and method/constructor code |
| **Metaspace** | Shared native memory in HotSpot | Primarily implements class-metadata storage; it is not identical to every aspect of the specification's Method Area |
| **Heap** | Shared | Storage from which class instances and arrays are allocated; also contains `java.lang.Class` objects in HotSpot |
| **JVM Stack** | Per thread | Frames for active Java method invocations |
| **PC register** | Per thread | Current JVM instruction address/position for a non-native method |
| **Native method stack** | Per thread if used by the implementation | Supports native methods and possibly a native implementation of the interpreter |
| **Code Cache** | Shared native HotSpot memory | JIT-compiled native machine code |

- A stack frame contains a local-variable array, operand stack, runtime constant-pool reference, and method return/exception support.
- A frame belongs logically to a thread's JVM Stack, **not the Heap**. A local reference in that frame can point to an object on the shared Heap.
- Thread-private local-variable storage does not make a referenced object thread-private. Multiple threads can hold local references to the same mutable object.
- `-Xmx` limits the Java Heap, not total process memory. Metaspace, code cache, thread stacks, direct buffers, GC structures, JNI/native allocations, and libraries consume additional memory.

## Execution engine

- **Interpreter:** Begins executing JVM bytecode with low startup cost and can gather runtime profiles. "Instruction by instruction" is a useful model, not a required implementation design.
- **JIT compiler:** Compiles selected methods or loop bodies into native machine code.
  - HotSpot normally uses adaptive tiered compilation with the interpreter, C1, and C2.
  - There is **no universal rule such as exactly 10,000 calls** before compilation; thresholds and policies are adaptive and configurable.
  - Hot loops can enter compiled code through on-stack replacement (OSR).
  - Compiled code can be invalidated and deoptimized if a speculative assumption becomes false.
- **Garbage collector:** Reclaims objects that are unreachable from GC roots.
  - GC is **not triggered only when Eden fills**. Triggers and phases depend on the collector and can include allocation pressure, occupancy thresholds, metadata pressure, explicit requests, and recovery/failure conditions.
  - GC reclaims unreachable object graphs, including unreachable cycles; "has no references" is an insufficient definition.
  - Young/old generations, Eden/Survivor spaces, promotion, and Minor/Major/Full terminology are collector-specific rather than universal JVM layouts.
  - Concurrent collectors still have some stop-the-world phases, but they do not pause application execution throughout the entire cycle.
  - GC does not prevent reachable-object memory leaks or guarantee that `OutOfMemoryError` cannot occur.

## Native methods

- The `native` modifier applies to **methods**, not variables.
- Native methods are implemented outside Java bytecode, often in C or C++, and commonly interact with Java through JNI.
- Native code can call operating-system or hardware APIs, but it also introduces platform dependence and memory/resource-safety risks.
- GC does not automatically close files, sockets, database connections, or arbitrary native resources. Use explicit lifecycle management and `try`-with-resources where applicable.

---

## Best Practices to Improve JVM Performance

- **Measure before tuning.** Use JFR/JMC, `jcmd`, VisualVM, a suitable third-party profiler such as YourKit, GC logs, application metrics, and representative load tests to identify the actual bottleneck.
- Avoid unnecessary allocation when profiling shows that allocation rate or GC pressure matters; do not sacrifice clarity for speculative object-reuse tricks.
- Treat static mutable state and long-lived static references deliberately because they can increase coupling and retain object graphs. Static state is not inherently a performance problem.
- Prefer narrow variable scope for clarity and easier reachability analysis, but do not assume a local reference makes the referenced object thread-safe.
- Do **not routinely assign local references to `null`**. Let scope and normal reachability do the work. Explicit nulling is occasionally useful for releasing a large object from a genuinely long-lived frame or clearing retained array/collection slots, but it should be justified by measurement or lifecycle needs.
- Start with the JVM's ergonomically selected collector. Choose G1, Parallel, Serial, ZGC, or a supported Shenandoah configuration only when measured throughput, latency, footprint, or CPU goals justify the change. CMS is removed from modern JDKs.
- Size `-Xms` and `-Xmx` from the measured live set, allocation behavior, service goals, and container/process limits. Leave headroom for non-heap/native memory. Remember that `-Xss` controls each platform thread's stack, not the Heap.
- Prepare diagnostics where appropriate: unified GC/safepoint logs, JFR, and carefully planned heap dumps on `OutOfMemoryError`. Dumps can be large and contain sensitive data.

For complete explanations, see [JVM Runtime Data Areas](ch1-jvm-runtime-data-areas-final.md), [Execution Engine](Ch2-Execution%20Engine.md), and [Java Memory Model](Ch3-java-memory-model-jmm-notes.md).
